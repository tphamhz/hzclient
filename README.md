# How to run

## 1. List all maps
`java -cp "target/lib/*:target/classes" -Dhazelcast.client.config=hazelcast-client.yaml com.tp.example.LocalClient`

Sample output:
```
Get all Maps
1. m1
2. m2
3. m3
```

## 2. Check Map EntryView TTL in a map
The application will examine N number of Map EntryView with N as configured -Dentry.fetch.size=<N>. If not specified, it uses default value DEFAULT_ENTRY_FETCH_SIZE = 100.
To not impact the cluster, N is automatically reduced to MAX_ENTRY_FETCH_SIZE = 1000 if the value provided N is greater than that.

`java -cp "target/lib/*:target/classes" -Dhazelcast.client.config=hazelcast-client.yaml -Dentry.fetch.size=100 -Dentry.print.details=true com.tp.example.LocalClient <map-name>`

Set `-Dentry.print.details=false` if you want to skip printing EntryView details

Note: The output between different runs of the same parameters could be different as the pool of EntryViews could be different.

Sample output:

```
Checking EntryView for map: m1
===================
Entry TTL | lastAccessTime | creationTime | expirationTime
Infinite | 2024-09-10 09:50:41 | 2024-09-09 18:22:03 | Infinite
3600 seconds | 2024-09-10 09:50:41 | 2024-09-10 09:50:05 | 2024-09-10 10:50:05
3600 seconds | 2024-09-10 09:50:41 | 2024-09-10 09:50:05 | 2024-09-10 10:50:05
...

=== Summary of EntryView <TTL settings, count> === 
{Infinite=15, 3600 seconds=330}
===================
```