package com.tp.example;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.core.DistributedObject;
import com.hazelcast.core.EntryView;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;


public class LocalClient {

    private HazelcastInstance client;
    private static int DEFAULT_ENTRY_FETCH_SIZE = 100;
    private static int MAX_ENTRY_FETCH_SIZE = 1000;

    public LocalClient(HazelcastInstance client){
        this.client = client;
    }

    public static void main(String[] args) {
        HazelcastInstance client = HazelcastClient.newHazelcastClient();
        LocalClient localClient = new LocalClient(client);

        if (args.length == 0) {
            //get all Map names
            List<String> allMapsName = localClient.getAllMapsName();
            localClient.printMap(allMapsName);
        } else {
            // get EntryView for X entries  for a map
            //if run with java MainClass <mapName>

            String mapName =  args[0].trim();
            int numOfEntries = localClient.getEntryFetchSizeFromProperties();
            localClient.processMapEntryView(mapName, numOfEntries);
        }

        client.shutdown();
    }

    private List<String> getAllMapsName(){
        Collection<DistributedObject> distributedObjects = this.client.getDistributedObjects();
        List<String> maps = distributedObjects.stream()
                .filter(d -> d.getServiceName().equals("hz:impl:mapService") && !d.getName().startsWith("__"))
                .map(d -> d.getName())
                .collect(Collectors.toList());

        Collections.sort(maps);
        return maps;
    }


    private  void printMap(List<String> maps) {
        System.out.println("Get all Maps");
        System.out.println("===================");
        for(int i=0; i<maps.size(); i++){
            System.out.println((i+1) +". " + maps.get(i));
        }
        System.out.println("===================");
    }


    private boolean isPrintEntryViewDetails(){
        String printDetailsFlag = System.getProperty("entry.print.details");
        if(printDetailsFlag==null || !"false".equalsIgnoreCase(printDetailsFlag))
            return true;
        return  false;
    }

    private int getEntryFetchSizeFromProperties(){
        int n = DEFAULT_ENTRY_FETCH_SIZE;
        String fetch_size = System.getProperty("entry.fetch.size");
        if(fetch_size == null)
            return n;
        try {
            n = Integer.parseInt(fetch_size);
            if(n > MAX_ENTRY_FETCH_SIZE) {
                System.out.println("To minimize impact on the cluster, max entry.fetch.size allowed to run is set to: "+ MAX_ENTRY_FETCH_SIZE);
                n = MAX_ENTRY_FETCH_SIZE;
            }

        } catch (Exception e) {
            System.out.println("Unable to parse the args entry.fetch.size = " + fetch_size + " to a number");
            System.out.println("Default entry.fetch.size to " + DEFAULT_ENTRY_FETCH_SIZE);
        }

        return n;
    }

    private  Map<String, Integer> processMapEntryView(String mapName, int numOfEntries){
        System.out.println("processMapEntryView for: "+ mapName + " fetch size: "+numOfEntries);

        IMap imap = client.getMap(mapName);
        Iterator<Map.Entry> iterator = imap.iterator(DEFAULT_ENTRY_FETCH_SIZE);
        EntryView entryView;
        Map<String, Integer> uniqueEntryTTL =  new HashMap<String, Integer>();
        boolean printEntryDetails = this.isPrintEntryViewDetails();

        System.out.println("Checking EntryView for map: "+mapName);
        System.out.println("===================");
        if(printEntryDetails) {
            System.out.println("Entry TTL | lastAccessTime | creationTime | expirationTime");
        }

        int entryCount = 0;
        while (iterator.hasNext() && entryCount < numOfEntries){
            ++entryCount;
            entryView = imap.getEntryView(iterator.next().getKey());
            String ttl_format = (entryView.getTtl() == Long.MAX_VALUE) ? "Infinite" : (entryView.getTtl()/1000 + " seconds");
            String expireTime_format =  (entryView.getTtl() == Long.MAX_VALUE) ? "Infinite" : formatSecondsToDate(entryView.getExpirationTime());
            String lastacessTime_format = (entryView.getLastAccessTime()==0) ? "N/A" : formatSecondsToDate(entryView.getLastAccessTime());

            if(printEntryDetails){
                System.out.println(ttl_format+ " | " + lastacessTime_format
                        +" | " + formatSecondsToDate(entryView.getCreationTime()) +" | "+ expireTime_format) ;
            }

            int count = uniqueEntryTTL.containsKey(ttl_format) ?  uniqueEntryTTL.get(ttl_format).intValue() +1 : 1;
            uniqueEntryTTL.put(ttl_format, count);

        }

        //print summary
        System.out.println("=== Summary of EntryView <TTL settings, count> ===");
        System.out.println(uniqueEntryTTL);
        System.out.println("===================");

        return uniqueEntryTTL;
    }


    public static String formatSecondsToDate(long seconds) {
        // Create an Instant from the seconds
        Instant instant = Instant.ofEpochMilli(seconds);

        // Define a formatter to convert the Instant to a string
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                .withZone(ZoneId.systemDefault());  // Use system's default timezone

        // Format the instant to a date string
        return formatter.format(instant);
    }

}