# builder
interface based

## supported types:



1. simple types: primitives, boxed primitives, String.
2. complex types:  List, Set of boxed types and String.
3. support for Optional for optional properties
    
## support for @Option.DefaultXX()
    support for default for both simple types and collection types
```java
   @Option.DefaultInt(18)//byte, short, int
    int minAge()

   @Option.DefaultDouble(221.212d)//float, double

   @Option.Default({"value1", "value2"})//char, String

   @Option.DefaultBoolean({})//boolean

```
## support for AddXX singular builder method for collection
    add methods to add single items to the builder instance before building
```java
   @Option.Singular
   List<String> words

   builder().addWord("")

```

