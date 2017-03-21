{% capture url %}http://bibliome.jouy.inra.fr/demo/alvisdb/obt{% endcapture %}

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

[**`/entities?fields=name&fields=root`**]({{url}}/entities?fields=name&fields=root)
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
