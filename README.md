# Kotlin Design Patterns Creator

![Build](https://github.com/bymaskeli53/KotlinDesignPatternsCreator/workflows/Build/badge.svg)

A JetBrains IDE plugin that injects classic object-oriented design patterns directly into the Kotlin class under your caret. Patterns are applied in place via PSI — no new files are generated, no string templates are pasted, and a single undo reverses the whole transformation.

## Supported Patterns

- **Builder** &mdash; turns the class's primary constructor private and adds a nested `Builder` that lets you decide per property whether it's required (constructor argument) or optional (chainable setter).
- **Factory** &mdash; for interfaces and abstract classes, generates a nested type enum, two stub implementations (with `override … = TODO()` for every abstract method), and a `create()` factory in a companion object.
- **Observer** &mdash; injects an `${ClassName}Observer` functional interface, an observer list, `add` / `remove` / `notifyObservers()`, and optionally appends `notifyObservers()` at the end of methods you pick from a dialog.
- **Strategy** &mdash; lets you pick one of the class's methods and extracts it as a `…Strategy` functional interface, replacing the method body with a delegating call.

## Requirements

- IntelliJ IDEA 2023.3 or newer (Community or Ultimate)
- Android Studio (any build based on the IntelliJ Platform 2023.3+)
- Java/Kotlin support in the IDE

## Installation

### From JetBrains Marketplace

1. Open <kbd>Settings</kbd> → <kbd>Plugins</kbd> → <kbd>Marketplace</kbd>.
2. Search for **"Kotlin Design Patterns Creator"**.
3. Click <kbd>Install</kbd> and restart the IDE when prompted.

### Manual install

1. Download the latest `KotlinDesignPatternsCreator-<version>.zip` from the [Releases page](https://github.com/bymaskeli53/KotlinDesignPatternsCreator/releases/latest), or build it locally with `./gradlew buildPlugin` (the zip lands in `build/distributions/`).
2. In the IDE: <kbd>Settings</kbd> → <kbd>Plugins</kbd> → <kbd>⚙️</kbd> → <kbd>Install plugin from disk…</kbd> and select the zip.
3. Restart the IDE.

## Usage

1. Open a `.kt` file.
2. Place the caret anywhere inside a class body.
3. Right-click → **Kotlin Design Patterns Creator** → pick the pattern you want.

Some patterns prompt for additional input (which methods to extract, which properties are required, and so on). The action is hidden when the file is not Kotlin, when the caret is not inside a class, or when the class is incompatible with the chosen pattern (e.g. Factory only appears on interfaces and abstract classes).

## Before / After Examples

### Builder

After running **Builder**, you pick which properties are required. In this example `name` and `email` stay required; `age` and `phone` are marked optional.

**Before**

```kotlin
class User(val name: String, val email: String, val age: Int, val phone: String)
```

**After**

```kotlin
class User private constructor(
    val name: String,
    val email: String,
    val age: Int?,
    val phone: String?
) {
    class Builder(
        private val name: String,
        private val email: String
    ) {
        private var age: Int? = null
        private var phone: String? = null

        fun age(age: Int) = apply { this.age = age }
        fun phone(phone: String) = apply { this.phone = phone }

        fun build(): User {
            return User(
                name = name,
                email = email,
                age = age,
                phone = phone
            )
        }
    }
}
```

Call site:

```kotlin
val user = User.Builder("Joe", "joe@example.com")
    .age(30)
    .build()
```

### Factory

Running **Factory** on an interface generates a nested type enum, two stub implementations with `override … = TODO()` for every abstract member, and a `create()` factory in a companion object.

**Before**

```kotlin
interface PaymentMethod {
    fun pay(amount: Double)
    fun refund(amount: Double)
}
```

**After**

```kotlin
interface PaymentMethod {
    fun pay(amount: Double)
    fun refund(amount: Double)

    enum class PaymentMethodType { IMPL_A, IMPL_B }

    class PaymentMethodImplA : PaymentMethod {
        override fun pay(amount: Double) = TODO()
        override fun refund(amount: Double) = TODO()
    }

    class PaymentMethodImplB : PaymentMethod {
        override fun pay(amount: Double) = TODO()
        override fun refund(amount: Double) = TODO()
    }

    companion object {
        fun create(type: PaymentMethodType): PaymentMethod = when (type) {
            PaymentMethodType.IMPL_A -> PaymentMethodImplA()
            PaymentMethodType.IMPL_B -> PaymentMethodImplB()
        }
    }
}
```

### Observer

After running **Observer**, a dialog lists the class's methods so you can pick which ones should fire `notifyObservers()` at the end. In this example `updatePrice` is selected.

**Before**

```kotlin
class StockPrice {
    var price: Double = 0.0

    fun updatePrice(newPrice: Double) {
        price = newPrice
    }
}
```

**After**

```kotlin
class StockPrice {
    var price: Double = 0.0

    fun updatePrice(newPrice: Double) {
        price = newPrice
        notifyObservers()
    }

    fun interface StockPriceObserver {
        fun onChanged()
    }

    private val observers = mutableListOf<StockPriceObserver>()

    fun addObserver(observer: StockPriceObserver) {
        observers.add(observer)
    }

    fun removeObserver(observer: StockPriceObserver) {
        observers.remove(observer)
    }

    private fun notifyObservers() {
        observers.forEach { it.onChanged() }
    }
}
```

### Strategy

Running **Strategy** opens a dialog listing the class's methods. The selected method's body is replaced with a delegation through a generated `…Strategy` interface.

**Before**

```kotlin
class SortingAlgorithm {
    fun sort(list: List<Int>): List<Int> {
        return list.sorted()
    }
}
```

**After** (selecting `sort`)

```kotlin
class SortingAlgorithm {
    fun interface SortStrategy {
        fun sort(list: List<Int>): List<Int>
    }

    private var sortStrategy: SortStrategy? = null

    fun setSortStrategy(strategy: SortStrategy) {
        this.sortStrategy = strategy
    }

    fun sort(list: List<Int>): List<Int> {
        return sortStrategy?.sort(list) ?: throw IllegalStateException("Strategy not set")
    }
}
```

## Building from Source

```bash
./gradlew buildPlugin     # produces build/distributions/KotlinDesignPatternsCreator-<version>.zip
./gradlew runIde          # launches a sandbox IDE with the plugin installed
./gradlew verifyPlugin    # runs the JetBrains plugin-structure verifier
```

The build targets `since-build = 233` and compiles against the IntelliJ Platform 2024.3 SDK with JVM 17.

## License

Released under the [MIT License](LICENSE). © 2026 Muhammet Gündoğar.
