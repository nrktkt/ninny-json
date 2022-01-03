# Automatic derivation for `ToJson` and `FromJson`

Rather than implementing `ToJson` and `FromJson` by hand, you can generate them automatically using

<sauce-code 
    repo='kag0/ninny-json'
    lang='scala'
    file='ninny/test/src-2/io/github/kag0/ninny/userguide/SemiAuto.scala'
    lines='11:16'
></sauce-code>

## Full-auto derivation for `ToJson` and `FromJson`

If you like you can even skip the declaration by mixing in `AutoToJson` or 
`AutoFromJson`, or importing `io.github.kag0.ninny.Auto._`.

<sauce-code 
    repo='kag0/ninny-json'
    lang='scala'
    file='ninny/test/src-2/io/github/kag0/ninny/userguide/FullAuto.scala'
    lines='9:18'
></sauce-code>

## Modifying field names with annotations

You can change the name of a field being read to/from JSON using the `@JsonName` annotation.

<sauce-code 
    repo='kag0/ninny-json'
    lang='scala'
    file='ninny/test/src-2/io/github/kag0/ninny/userguide/Annotations.scala'
    lines='10:19'
></sauce-code>

## Using default values for optional fields

If your case class has optional parameters then you can use their default values when a field is absent by importing the feature flag `FromJsonAuto.useDefaults` in the scope of the derivation for your `FromJson`.

<sauce-code 
    repo='kag0/ninny-json'
    lang='scala'
    file='ninny/test/src-2/io/github/kag0/ninny/userguide/DefaultValues.scala'
    lines='9:17'
></sauce-code>
