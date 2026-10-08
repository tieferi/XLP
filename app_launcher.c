// SPDX-License-Identifier: GPL-3.0-or-later

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <stdbool.h>
#include <limits.h>

#include <sys/types.h>
#include <sys/sysctl.h>
#include <unistd.h>
#include <sys/stat.h>

#include <mach-o/dyld.h>

const int ARG_MAX_COUNT = 100;

static bool str_empty(const char *s) {
    return s[0] == '\0';
}

static bool str_add(char *buf, size_t buf_len, const char *s) {
    if (strlen(buf) + strlen(s) >= buf_len) {
        return false;
    }
    strcat(buf, s);
    return true;
}

static char *str_cut(char *buf, char c, const char *s) {
    char *p = strrchr(buf, c);
    if (!p) {
        return NULL;
    }
    *p++ = '\0';
    if (s && strcmp(p, s) != 0) {
        return NULL;
    }
    return p;
}

static bool str_replace(char *buf, size_t buf_len, const char *s1, const char *s2) {
    size_t l = strlen(buf), l1 = strlen(s1), l2 = strlen(s2);
    char *p = buf;
    while (true) {
        p = strstr(p, s1);
        if (!p) {
            break;
        }
        l = l - l1 + l2;
        if (l >= buf_len) {
            return false;
        }
        char *q1 = p + l1, *q2 = p + l2;
        memmove(q2, q1, strlen(q1) + 1);
        memcpy(p, s2, l2);
        p = q2;
    }
    return true;
}

static bool file_exists(const char *path) {
    struct stat info;
    return stat(path, &info) == 0 && S_ISREG(info.st_mode);
}

static bool read_line(FILE *f, char *buf, int buf_len, bool *end) {
    int l = 0;
    while (true) {
        int c = fgetc(f);
        if (c == '\n') {
            break;
        } else if (c == EOF) {
            *end = true;
            break;
        }
        if (l >= buf_len - 1) {
            return false;
        }
        buf[l++] = c;
    }
    buf[l] = '\0';
    return true;
}

static bool get_app_info(char *bundle_path, char *app_name) {
    char buf1[PATH_MAX];
    uint32_t buf_size = PATH_MAX;
    if (_NSGetExecutablePath(buf1, &buf_size) != 0) {
        return false;
    }
    char buf2[PATH_MAX];
    if (realpath(buf1, buf2) == NULL) {
        return false;
    }
    char *launcher_name;
    if (!(launcher_name = str_cut(buf2, '/', NULL))
        || !str_cut(buf2, '/', "MacOS")
        || !str_cut(buf2, '/', "Contents")) {
        return false;
    }
    strcpy(bundle_path, buf2);
    char *bundle_name;
    if (!(bundle_name = str_cut(buf2, '/', NULL))
        || !str_cut(bundle_name, '.', "app")
        || (strcmp(bundle_name, launcher_name) != 0))
        return false;
    strcpy(app_name, bundle_name);
    return true;
}

static bool get_java_file(char *bundle_path, char *java_file) {
    const char *rel_path = NULL;
    int value = 0;
    size_t size = sizeof(int);
    if (sysctlbyname("hw.optional.x86_64", &value, &size, NULL, 0) == 0 && value != 0) {
        rel_path = "/Contents/jre_x86_64/Home/bin/java";
    } else if (sysctlbyname("hw.optional.arm64", &value, &size, NULL, 0) == 0 && value != 0) {
        rel_path = "/Contents/jre_arm64/Home/bin/java";
    } else {
        return false;
    }
    char buf[PATH_MAX] = "";
    if (!str_add(buf, PATH_MAX, bundle_path)
        || !str_add(buf, PATH_MAX, rel_path)
        || !file_exists(buf)) {
        return false;
    }
    strcpy(java_file, buf);
    return true;
}

const int JAVA_OPTION_MAX = 500;
const int JAVA_OPTION_MAX_COUNT = 20;
const int CLASSPATH_MAX = 20000;
const int MAINCLASS_MAX = 100;

struct cfg_values {
    char java_options[JAVA_OPTION_MAX_COUNT][JAVA_OPTION_MAX];
    int java_option_count;
    char classpath[CLASSPATH_MAX];
    char mainclass[MAINCLASS_MAX];
};

static bool add_java_option(struct cfg_values *cfg_values, const char *s) {
    if (cfg_values->java_option_count >= JAVA_OPTION_MAX_COUNT
        || strlen(s) >= JAVA_OPTION_MAX) {
        return false;
    }
    strcpy(cfg_values->java_options[cfg_values->java_option_count++], s);
    return true;
}

static bool add_to_classpath(struct cfg_values *cfg_values, const char *s) {
    if (!str_empty(cfg_values->classpath)) {
        if (!str_add(cfg_values->classpath, CLASSPATH_MAX, ":")) {
            return false;
        }
    }
    if (!str_add(cfg_values->classpath, CLASSPATH_MAX, s)) {
        return false;
    }
    return true;
}

static bool set_mainclass(struct cfg_values *cfg_values, const char *s) {
    if (!str_empty(cfg_values->mainclass)
        || strlen(s) >= MAINCLASS_MAX) {
        return false;
    }
    strcpy(cfg_values->mainclass, s);
    return true;
}

static bool read_cfg_file(const char *cfg_file, struct cfg_values *cfg_values) {
    FILE *f = fopen(cfg_file, "r");
    if (!f) {
        return false;
    }
    const int BUF_LEN = 1000;
    char buf[BUF_LEN];
    bool end = false, err = false;
    while (!end && !err) {
        if (!read_line(f, buf, BUF_LEN, &end)) {
            break;
        }
        char *p = strchr(buf, '=');
        if (p) {
            *p++ = '\0';
            if (strcmp(buf, "java-options") == 0) {
                if (!add_java_option(cfg_values, p)) {
                    err = true;
                }
            } else if (strcmp(buf, "app.classpath") == 0) {
                if (!add_to_classpath(cfg_values, p)) {
                    err = true;
                }
            } else if (strcmp(buf, "app.mainclass") == 0) {
                if (!set_mainclass(cfg_values, p)) {
                    err = true;
                }
            }
        }
    }
    fclose(f);
    return end && !err && !str_empty(cfg_values->mainclass);
}

int main(int argc, const char **argv) {

    char bundle_path[PATH_MAX], app_name[NAME_MAX];
    if (!get_app_info(bundle_path, app_name)) {
        fprintf(stderr, "Error locating app bundle\n");
        return 11;
    }

    char java_file[PATH_MAX];
    if (!get_java_file(bundle_path, java_file)) {
        fprintf(stderr, "Error locating Java executable\n");
        return 21;
    }

    char cfg_file[PATH_MAX] = "";
    if (!str_add(cfg_file, PATH_MAX, bundle_path)
        || !str_add(cfg_file, PATH_MAX, "/Contents/app/")
        || !str_add(cfg_file, PATH_MAX, app_name)
        || !str_add(cfg_file, PATH_MAX, ".cfg")
        || !file_exists(cfg_file)) {
        fprintf(stderr, "Error locating config file\n");
        return 31;
    }
    struct cfg_values cfg_values = {0};
    if (!read_cfg_file(cfg_file, &cfg_values)) {
        fprintf(stderr, "Error reading config file\n");
        return 32;
    }
    char app_dir[PATH_MAX] = "";
    if (!str_add(app_dir, PATH_MAX, bundle_path)
        || !str_add(app_dir, PATH_MAX, "/Contents/app")) {
        fprintf(stderr, "Error building 'app' path\n");
        return 33;
    }
    if (!str_replace(cfg_values.classpath, CLASSPATH_MAX, "$APPDIR", app_dir)) {
        fprintf(stderr, "Error resolving classpath\n");
        return 34;
    }

    char icon[PATH_MAX] = "";
    if (!str_add(icon, PATH_MAX, bundle_path)
        || !str_add(icon, PATH_MAX, "/Contents/Resources/")
        || !str_add(icon, PATH_MAX, app_name)
        || !str_add(icon, PATH_MAX, ".icns")) {
        fprintf(stderr, "Error building icon path\n");
        return 41;
    }
    if (file_exists(icon)) {
        char icon_option[JAVA_OPTION_MAX] = "";
        if (!str_add(icon_option, JAVA_OPTION_MAX, "-Xdock:icon=")
            || !str_add(icon_option, JAVA_OPTION_MAX, icon)
            || !add_java_option(&cfg_values, icon_option)) {
            fprintf(stderr, "Error building icon path\n");
            return 42;
        }
    }

    int arg_count = 1 // java_file
        + cfg_values.java_option_count // java_options
        + (!str_empty(cfg_values.classpath) ? 2 : 0) // "-cp", classpath
        + 1 // mainclass
        + (argc - 1) // argv[j], j > 0
        + 1; // NULL
    if (arg_count > ARG_MAX_COUNT) {
        fprintf(stderr, "Argument limit exceeded\n");
        return 51;
    }
    char *args[ARG_MAX_COUNT] = {0};
    int i = 0;
    args[i++] = java_file;
    for (int j = 0; j < cfg_values.java_option_count; j++) {
        args[i++] = cfg_values.java_options[j];
    }
    if (!str_empty(cfg_values.classpath)) {
        args[i++] = "-cp";
        args[i++] = cfg_values.classpath;
    }
    args[i++] = cfg_values.mainclass;
    for (int j = 1; j < argc; j++) {
        args[i++] = (char*)argv[j];
    }
    args[i] = NULL;

    execv(java_file, args);
    perror("execv");
    return 1;
}
