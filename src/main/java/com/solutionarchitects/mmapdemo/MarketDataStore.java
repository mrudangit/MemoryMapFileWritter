package com.solutionarchitects.mmapdemo;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Random;


@Service
public class MarketDataStore {


    private static Random random = new Random();


    @Value("${numOfRecords}")
    public int numOfRecords;

    @Value("${updatePercent}")
    public int updatePercent;
    private ArrayList<MarketData> listOfMarketData= new ArrayList<>();


    @PostConstruct
    protected void Init(){

        for(int i=0;i < this.numOfRecords; i++){

            MarketData md = MarketData.createInstance(i);
            md.generateMarketData();
            listOfMarketData.add(i,md);

        }
    }

    public MarketData getMarketData(int i){
        return listOfMarketData.get(i);
    }

    public MarketData updateRandomMarketData(){



        int mdIndex = random.nextInt(listOfMarketData.size());

        MarketData m = this.listOfMarketData.get(mdIndex);
        m.generateMarketData();

        return m;

    }

}
