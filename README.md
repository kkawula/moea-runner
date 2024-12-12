# Moea framework tools

# How to setup the environment

> Shell snippets are adjusted for Linux and MacOS. But there is always `.bat` script alternative for Windows.


## 1. Build Binaries

```shell
./gradlew createBinaries
```

This task build each package (cli and server), copy binaries and all dependencies to `<package-name>/build/install/<application-name>/bin` directory.

## 2. Run the server

To start server with created binary, run the following command:
```shell
./server/build/install/moea-server/bin/moea-server 
```

## 3. Run the CLI

```shell
./cli/build/install/moea-client/bin/moea-client experiment-create --evaluations 2000 --algorithms NSGAII,GDE3 --problems UF1,DTLZ2 --metrics Hypervolume,Spacing
```

```shell
./cli/build/install/moea-client/bin/moea-cli experiments-list
```

```shell
./cli/build/install/moea-client/bin/moea-client experiment-status 1
```

```shell
./cli/build/install/moea-client/bin/moea-client experiments-list
```

## 4. Clean the environment

```shell
./gradlew clean
```
