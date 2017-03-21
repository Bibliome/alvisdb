# API

## Entity types

```
/entities
```

Return all known entity types.

### Parameters

| Parameter | Source | Type | Effect |
|:---------:|:------:|:----:|:-------|
| `fields` | Query | `String[]` | `id` `name` `root` `all` |


### Examples

[**`/entities?fields=name&fields=root`**](http://bibliome.jouy.inra.fr/demo/alvisdb/obt/entities?fields=name&fields=root)
```json
[
    {
        "id": "habitat",
        "name": "Habitat",
        "root": "OBT:000000"
    },
    {
        "id": "taxon",
        "name": "Taxon",
        "root": "2"
    }
]
```
