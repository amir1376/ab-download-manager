#include <errno.h>
#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>

int main(int argc, char *argv[]) {
    if (argc < 2) {
        fprintf(stderr, "Usage: %s program [argument ...]\n", argv[0]);
        return 1;
    }

    if (setsid() == -1) {
        if (errno != EPERM) {
            perror("setsid");
            return 1;
        }

        pid_t pid = fork();
        if (pid < 0) {
            perror("fork");
            return 1;
        }

        if (pid > 0) {
            _exit(0);
        }

        if (setsid() == -1) {
            perror("setsid");
            return 1;
        }
    }

    execvp(argv[1], &argv[1]);

    perror(argv[1]);
    return 127;
}