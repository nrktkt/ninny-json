There are a few finer points around handling numbers in JSON and in ninny.
Below are several details to note for edge cases.

## Precision

The JSON standard doesn't specify a precision for JSON numbers, but recommends compatibility with IEEE 754 double precision floats.  
For this reason ninny parses numbers into `JsonDouble` (which wraps `Double`) by default. 
For arbitrary precision use `Json.parse(string, highPrecision = true)` to parse into `JsonDecimal` (which wraps `BigDecimal`) instead.  
If you try to parse a `JsonDouble` into `BigDecimal` (eg. with `JsonDouble(1.23).to[BigDecimal]`) ninny will log a warning.  
If you try to parse an oversized `JsonDecimal` into `Double` you'll get infinity or negative infinity.

## NaN and non-finite numbers

IEEE 754 allows for positive and negative infinity and not-a-number values. JSON does not.
ninny doesn't want to throw exceptions when converting Scala objects to JSON AST or when converting JSON AST to JSON string.
So when converting these values in `JsonDouble` ninny will write them as JSON strings.
The opposite is not true of parsing, and parsing will fail attempting to read these types of string values into numbers 
(unless you add support explicitly). 

Generally it is better to avoid relying on assumed functionality when dealing with these values and explicitly implement your own behavior.