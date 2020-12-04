package com.example.apigateway.config;

import io.github.bucket4j.distributed.proxy.ClientSideConfig;
import io.github.bucket4j.distributed.proxy.generic.compare_and_swap.AbstractCompareAndSwapBasedBackend;
import io.github.bucket4j.distributed.proxy.generic.compare_and_swap.CompareAndSwapBasedTransaction;
import org.redisson.api.RMap;


import java.util.Optional;

public class RMapBasedRedissonBackend extends AbstractCompareAndSwapBasedBackend<String> {

    public RMap<String, byte[]> getBuckets() {
        return buckets;
    }

    private final RMap<String, byte[]> buckets;

    public RMapBasedRedissonBackend(RMap<String, byte[]> buckets, ClientSideConfig clientSideConfig)  {
        super(clientSideConfig);
        this.buckets = buckets;
    }

    @Override
    protected CompareAndSwapBasedTransaction allocateTransaction(String key) {
        return new RedissonCompareAndSwapTransaction(key);
    }

    @Override
    protected void releaseTransaction(CompareAndSwapBasedTransaction transaction) {
        // do nothing
    }



    private class RedissonCompareAndSwapTransaction implements CompareAndSwapBasedTransaction {

        private final String key;

        private RedissonCompareAndSwapTransaction(String key) {
            this.key = key;
        }


        @Override
        public Optional<byte[]> get() {
            byte[] persistedState = buckets.get(key);
            return Optional.ofNullable(persistedState);
        }

        @Override
        public boolean compareAndSwap(byte[] originalData, byte[] newData) {
            if (originalData == null) {
                // Redisson prohibits the usage null as values, so "replace" must not be used in such cases
                return buckets.putIfAbsent(key, newData) == null;
            }
            return buckets.replace(key, originalData, newData);
        }

    }

}