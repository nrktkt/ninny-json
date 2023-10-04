# Automatic derivation for `ToJson` and `FromJson`

Rather than implementing `ToJson` and `FromJson` by hand, you can generate them automatically using

<sauce-code 
    repo='nrktkt/ninny-json'
    lang='scala'
    file='ninny/test/src-2/nrktkt/ninny/userguide/SemiAuto.scala'
    lines='11:16'
></sauce-code>

## Full-auto derivation for `ToJson` and `FromJson`

If you like you can even skip the declaration by mixing in `AutoToJson` or 
`AutoFromJson`, or importing `nrktkt.ninny.Auto._`.

<sauce-code 
    repo='nrktkt/ninny-json'
    lang='scala'
    file='ninny/test/src-2/nrktkt/ninny/userguide/FullAuto.scala'
    lines='9:18'
></sauce-code>

## Modifying field names with annotations

You can change the name of a field being read to/from JSON using the `@JsonName` annotation.

<sauce-code 
    repo='nrktkt/ninny-json'
    lang='scala'
    file='ninny/test/src-2/nrktkt/ninny/userguide/Annotations.scala'
    lines='10:19'
></sauce-code>

## Using default values for optional fields

If your case class has optional parameters then you can use their default values when a field is absent by importing the feature flag `FromJsonAuto.useDefaults` in the scope of the derivation for your `FromJson`.

<sauce-code 
    repo='nrktkt/ninny-json'
    lang='scala'
    file='ninny/test/src-2/nrktkt/ninny/userguide/DefaultValues.scala'
    lines='9:17'
></sauce-code>

## Handling null references

We don't usually expect to have null references in Scala. So the default behavior is for null references to be passed to `ToJson` instances where they could be handled for a specific type.   
However if you need to handle null references generally we have [NullPointerBehavior](ninny/src-2/nrktkt/ninny/NullPointerBehavior.scala).  
By providing an instance of `NullPointerBehavior` in the scope of your `ToJson` derivation you can control if null references are passed to `ToJson` instances or if a value is provided instead. `NullPointerBehavior.Ignore` and `IgnorePointerBehavior.WriteNull` are provided the typical uses cases.

<sauce-code 
    repo='nrktkt/ninny-json'
    lang='scala'
    file='ninny/test/src-2/nrktkt/ninny/example/Userguide.scala'
    lines='207:214'
></sauce-code>
