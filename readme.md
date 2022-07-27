# Module RecyclerSpinner

RecyclerSpinner is a flexible and unopinionated spinner implementation that uses an internal spinner view to allow for dynamic spinner content. This is useful in situations where spinner entries may change while the spinner is displayed, such as when the spinner contents are retrieved over the network. It also supports expandable elements in the spinner and provides smooth default animations.

## Getting Started

To get started, make sure you have included the Maven Central repository, and then add it as a dependency to the target you intend to use it from, like so:

```groovy

dependencies {
    ..
    implementation 'co.luoja:recycler-spinner:1.2.0'
    ..
}

```

Once you have added the depencency, add the `RecyclerSpinner` to your layout and define a custom `RecyclerSpinnerAdapter` with the type of data you want to display. For a simple spinner without expandable sections, then you can inherit from `FlatSpinnerAdapter`. Here is an example.
```kotlin

class MySpinnerAdapter: FlatSpinnerAdapter<MyType, ..>(MyDiffer())

    class MyDiffer: DiffUtil.ItemCallback<MyType>() {
    
        override fun areContentsTheSame(oldItem: MyType, newItem: MyType): Boolean {
            // Return whether oldItem and newItem share the same data.
    }
    
        override fun areItemsTheSame(oldItem: MyType, newItem: MyType): Boolean {
            // Return whether oldItem and newItem have the identity.
        }
        
    }
    ..
}

```

You can also provide an initially selected value in the constructor, like so:

```kotlin
class MySpinnerAdapter(initiallySelected: MyType?): FlatSpinner<..>(initiallySelected, MyDiffer()) {...}

```

Then, to display items, override the functions to generate and populate view holders for items in the dropdown and the spinner itself, as you would when creating an adapter for a plain recycler view.

```kotlin
    ..
    override fun onCreateSelectedItemViewHolder(parent: ViewGroup, viewType: ViewType): SVH {
        // Create the view holder to be display the current selection.
    }
    
    abstract fun onBindSelectedItem(viewHolder: SVH, selectedItem: I?) {
        // Display the current selection using the view holder's itemView.
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: ViewType): DVH {
        // Create a view holder to display items in the dropdown
    }
    
    override fun onBindViewHolder(holder: DVH, item: MyType) {
        // Display the item in the dropdown using the view holder's itemView.
    }
    ..
```

Then, populate the spinner by setting its adapter to an instance of your adpater, calling `submitList` on the adapter. You can then observe  changes in selection by registering a callback `setOnItemSelectedListener`. Here is an example:

```kotlin

class MyFragment: Fragment() {
    ...
    override fun onViewCreated(view: View, bundle: Bundle?) {
        ...
        adapter = MySpinnerAdapter()
        
        spinner.adapter = adapter
        
        adapter.submitList(options)
        adapter.setOnItemSelectedListener() { selected ->
            // Respond to the new selection.
        }
        ..
    }
    ..
}
```

## Setting Padding and Spacing in the Dropdown

The padding around the dropdown's content and the spacing between dropdown elements can be set using xml attributes. To set the padding, use the attribute `app:popup_padding` to padding on all sides, or set padding on each side using attributes `app:popup_padding_start`, `app:popup_padding_top`, `app:popup_padding_end`, `app:popup_padding_bottom`. To set spacing, use the attribute `app:popup_spacing`. These are all dimension values, so you can use values like `6dp`.

Here is an example that will set the padding on all sides and the spacing between the elements to 8dp:

```xml
..
    <co.luoja.recyclerspinner.RecyclerSpinner
        ..
        app:popup_padding="8dp"
        app:popup_spacing="8dp"
        ../>
..
```

## Using Multiple Kinds of View

As with any other `RecyclerViewAdapter`, `RecyclerSpinnerAdapter` can be configured to use multiple kinds of views depending on the item being displayed. This is done separately for the views in the dropdown and the spinner itself. `RecyclerSpinnerAdapter` uses an interface to define viewtypes, rather than a raw integer. Here is an example:

```kotlin
class MySpinnerAdapter: FlatSpinnerAdapter<MySuperType, ..> {
    ..
    object MyFirstViewType: ViewType
    object MySecondViewType: ViewType
    
    override fun getItemViewType(element: MySuperType): ViewType {
        return when (element) {
            is MyFirstType -> MyFirstViewType
            is MySecondViewType -> MySecondViewType
        }
    }
    ..
}
```

Then, adjust the logic of the view creating and binding to reflect the different view types, like so:

```kotlin
    ..
    class MyFirstViewHolder..
    class MySecondViewHolder..

    override fun onCreateViewHolder(parent: ViewGroup, viewType: ViewType): DVH {
        return when (viewType) {
            is MyFirstViewType -> MyFirstViewHolder(...)
            is MySecondViewType -> MySecondViewHolder(...)
        }
    }
    
    override fun onBindViewHolder(holder: DVH, item: MyType) {
        if (holder is MyFirstViewHolder && item is MyFirstType) {
            // Binding logic for the first type
        } else if (holder is MySecondViewHolder && item is MySecondType) {
            // Binding logic for the second type
        }
    }
    ..
```

## Nested Dropdowns

RecyclerSpinner has built-in support for nested spinners using `NestedSpinnerAdapter`. To use this, you will need to provide types for items and for section identifiers, and define a `Differ` that handles each of these types, similar to a `DiffUtil.ItemCallback`. Here is an example:

```kotlin

class MyItem..
class MySectionIdentifier..

class MyNestedSpinnerAdapter: NestedSpinnerAdapter<MyItem, MySectionIdentifier, ..>(MyDiffer()) {

    class MyDiffer: NestedSpinnerAdapter<MyItem, MySectionIdentifier>() {
    
        override fun doSectionIdsShareIdentity(a: MySectionIdentifier, b: MySectionIdentifier): Boolean {
            // Return whether the identifiers represent the same section.
        }

        override fun doSectionIdsShareContents(a: MySectionIdentifier, b: MySectionIdentifier): Boolean {
            // Return whether the identifiers have the same data.
        }

        override fun doItemsShareIdentity(a: MyItem, b: MyItem): Boolean {
            // Return whether the items represent the same thing.
        }

        override fun doItemsShareContents(a: MyItem, b: MyItem): Boolean {
            // Return whether the items have the same data.
        }
    
    }
    ..
}
```

Then, define methods to create view holders for both sections and items, and to update these view holders based on the section identifiers and items, respectively. Here is an example:

```kotlin
    ..
    override fun onCreateSectionHeaderViewHolder(
        parent: ViewGroup,
        viewType: SectionViewType
    ): SectionHeaderHolder {
        // Create a section header view.
    }

    override fun onCreateItemViewHolder(
        parent: ViewGroup,
        viewType: ItemViewType
    ): ItemHolder {
        // Create an item view.
    }

    override fun onBindSectionHeaderViewHolder(
        viewHolder: SectionHeaderHolder,
        sectionIdentifier: MySectionIdentifier,
        expanded: Boolean
    ) {
        // Update the section header to reflect the identifier's contents and the expansion state.
    }

    override fun onBindItemViewHolder(
        viewHolder: ItemHolder,
        item: MyItem,
        sectionIdentifier: MySectionIdentifier?
    ) {
        // Update the view to reflect the new item.
    }
    ..
```

Data should be provided to the adapter via the method `NestedSpinnerAdapter.submitNestedList` as a `NestedList`, which can be constructed using `nestedListOf()`, with the items defined via `item`
and `section`. For example:

```kotlin

class MyFragment: Fragment() {
    ..
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        ..
        adapter = MyNestedSpinner()
        spinner.adapter = adapter
        
        adapter.submitNestedList(
            item(myFirstItem),
            item(mySecondItem),
            section(myFirstSectionIdentifier, myThirdItem, myFourthItem, myFifthItem),
            section(mySecondSectionIdentifier, mySecondSectionItems)
        )
        ..
    }
    ..
}
```

The adapter can be configured to use multiple view types in a similar way as described in the section Using Multiple Kinds of Views.

# Package co.luoja.recyclerspinner 

The base package for this library.

# Package co.luoja.recyclerspinner.adapter

The package containing adapters for populating recycler spinners.

