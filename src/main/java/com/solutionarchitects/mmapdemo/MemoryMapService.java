package com.solutionarchitects.mmapdemo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.util.EnumSet;

@Service
public class MemoryMapService {


    private Logger logger = LoggerFactory.getLogger(this.getClass().getName());


    @Value("${memorymapfilename}")
    private String memoryMapFileName;

    @Value("${numOfRecords}")
    public int numOfRecords;

    @Value("${updateInterval}")
    protected int updateInterval;

    @Value("${updatePercent}")
    public int updatePercent;


    private MappedByteBuffer mappedByteBuffer;

    private int counter = 0;

    private final MarketDataStore marketDataStore;

    public MemoryMapService(MarketDataStore marketDataStore) {
        this.marketDataStore = marketDataStore;
    }

    @PostConstruct
    protected void Init() throws IOException {

        int lengthOfFile = numOfRecords * MarketData.SIZE;
        int updateCount = (int) Math.floor((numOfRecords/updatePercent));

        logger.info("Memory Map File Size : {} ", lengthOfFile);

        Path p = Paths.get(memoryMapFileName);

        FileChannel mmapChannel = (FileChannel) Files.newByteChannel(p, EnumSet.of(StandardOpenOption.READ, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING));



        mappedByteBuffer = mmapChannel.map(FileChannel.MapMode.READ_WRITE, 0, lengthOfFile);
        mappedByteBuffer.order(ByteOrder.LITTLE_ENDIAN);

        logger.info("Memory Map File Size : {}", mappedByteBuffer.limit());

        populateMMAPFile();
        startUpdating(updateCount);




    }

    private void populateMMAPFile(){

        for(int i=0;i< marketDataStore.numOfRecords;i++){
            MarketData m = this.marketDataStore.getMarketData(i);
            writeToMMAPFile(m);
        }

    }

    private void writeToMMAPFile(MarketData m) {
        byte[] bytes = m.toBinary();
        int startIndex = m.index*MarketData.SIZE;
        mappedByteBuffer.position(startIndex);
        mappedByteBuffer.put(bytes,0, bytes.length);
    }

    private void startUpdating(int updateCount) {
        Flux.interval(Duration.ofMillis(updateInterval)).subscribe(aLong -> {

            for(int i=0;i < updateCount; i++) {
                MarketData m = this.marketDataStore.updateRandomMarketData();
                writeToMMAPFile(m);
            }


        });
    }


}
