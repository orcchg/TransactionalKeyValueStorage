Welcome to a Key-Value Storage !

Note for code reviewers: the assignment statement mentions that the
storage should be extendable, if possible. But the database can be
implemented in memory. I'm using `kotlin-coroutines` and make database
and storage methods `suspend` because in case database will be replaced
with persistent or remote implementation, its operations will become
blocking I/O operations. For the purpose of extensibility and easy
adoption, methods were made `suspend`, although for the current solution
it is not necessary. Also, the solution doesn't use any 3rd-party
libraries and `kotlin-coroutines` is the 1st-party library. Thank you.

## Usage
Please use the following commands for input:

 * SET <key> <value>   : stores the value for the key
 * GET <key>           : returns the current value for the key
 * DELETE <key>        : removes the entry for the key
 * COUNT <value>       : returns the number of keys that have the given value
 * BEGIN               : starts a new transaction
 * COMMIT              : completes the current transaction
 * ROLLBACK            : reverts to a state prior to BEGIN call
 * HELP                : displays usage
 * EXIT                : gracefully stops the process and exits

## Build & Run

#### with IntelliJ IDEA IDE

Simply import the project as Gradle project and run
`com.orcchg.trustwallet.task.presentation.cli.CliMainKt` class
from the `TrustWalletTask.main` module.

#### with gradle from CLI
```
./gradlew :classes :testClasses
./gradlew :run --console=plain --quiet
```

For some reason the execution may have poor performance.

### alternatively - with kotlin from CLI
```
brew install kotlin
./gradlew build
```

Then indicate absolute path to `kotlinx-coroutines-core-jvm-1.8.1.jar` on your local machine and run:
```
kotlin -cp ./build/libs/TrustWalletTask-1.0.jar:$HOME/.gradle/caches/modules-2/files-2.1/org.jetbrains.kotlinx/kotlinx-coroutines-core-jvm/1.8.1/bb0e192bd7c2b6b8217440d36e9758e377e450/kotlinx-coroutines-core-jvm-1.8.1.jar com.orcchg.trustwallet.task.presentation.cli.CliMainKt
```

## Tests
```
./gradlew :test
```
