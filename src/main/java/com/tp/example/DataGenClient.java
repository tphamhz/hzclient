package com.tp.example;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.config.MapConfig;
import com.hazelcast.core.DistributedObject;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import com.hazelcast.map.LocalMapStats;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;


public class DataGenClient {

    private static HazelcastInstance client1;

    public static void main(String[] args) {
        client1 =  HazelcastClient.newHazelcastClient();
        String[] maps = {"m1", "m2", "m3"};

        for (String m : maps) {
            gen_mapdata_withTTL(m, 3600);
        }


        IMap randomMap;
        for(String m: maps){
            randomMap = client1.getMap(m);
            for(int i=1; i<100; i++){
                //randomMap.get(i);
                randomMap.put(i, "v::"+i);
            }
        }


        client1.shutdown();
    }

    public static void gen_mapdata(String name){
        IMap randomMap =  client1.getMap(name);
        int count = 100 + new Random().nextInt(1000);
        HashMap map = new HashMap<Integer, String>();
        for(int i=1; i<=count; i++){
           map.put(i, "v" + i);
        }
        randomMap.putAll(map);
    }

    public static void gen_mapdata_withTTL(String name, int TTLinSeconds){
        IMap randomMap =  client1.getMap(name);
        int count = 100 + new Random().nextInt(1000);
        for(int i=1; i<=count; i++){
            randomMap.putAsync(i, "v" + i, TTLinSeconds, TimeUnit.SECONDS);
        }

    }

    public static void gen_mapdata_custom(String mapName){
        MapConfig mapConfig = new MapConfig(mapName);
        mapConfig.setTimeToLiveSeconds(1800);


    }
}