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
./cli/build/install/moea-client/bin/moea-client experiment-create --evaluations 2000 --algorithms NSGAII,GDE3 --problems UF1,DTLZ2_2 --metrics Hypervolume,Spacing --invocations 2
```

```shell
./cli/build/install/moea-client/bin/moea-client experiments-list --experiment-ids 1,2 --algorithm-name NSGAII --metric-name Hypervolume --problem-name UF1 --status FINISHED --from-date "1410-01-01 11:59:59" --to-date "2077-01-01 11:59:59"
```

```shell
./cli/build/install/moea-client/bin/moea-client experiment-status 1
```

```shell
./cli/build/install/moea-client/bin/moea-client experiment-results 1
```

```shell
./cli/build/install/moea-client/bin/moea-client experiment-repeat 1 --invocations 2
```

```shell
./cli/build/install/moea-client/bin/moea-client unique-experiments
```

```shell
./cli/build/install/moea-client/bin/moea-client aggregated-experiments-results 1 2 --from-date "1410-01-01 11:59:59" --to-date "2077-01-01 11:59:59"
```

```shell
./cli/build/install/moea-client/bin/moea-client aggregated-experiments-results 1 2 --from-date "1410-01-01 11:59:59" --to-date "2077-01-01 11:59:59" --output PLOT --file-name plot
```

```shell
./cli/build/install/moea-client/bin/moea-client aggregated-experiments-results 1 2 --from-date "1410-01-01 11:59:59" --to-date "2077-01-01 11:59:59" --output CSV --file-name data
```

```shell
./cli/build/install/moea-client/bin/moea-client group-name-update "new group name" --experiment-ids 2,3 --from-date "1410-01-01 11:59:59" --to-date "2077-01-01 11:59:59"
```

```shell
./cli/build/install/moea-client/bin/moea-client experiment-delete 1
```

```shell
./cli/build/install/moea-client/bin/moea-client group-delete "new-group-name"
```

## 4. Clean the environment

```shell
./gradlew clean
```
