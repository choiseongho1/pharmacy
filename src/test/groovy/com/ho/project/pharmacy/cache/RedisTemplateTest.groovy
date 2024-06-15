package com.ho.project.pharmacy.cache

import com.ho.project.AbstractIntegrationContainerBaseTest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.redis.core.HashOperations
import org.springframework.data.redis.core.RedisTemplate


class RedisTemplateTest extends AbstractIntegrationContainerBaseTest {

    @Autowired
    private RedisTemplate redisTemplate;

    def "RedisTemplate String operations"(){
        given :
        def valueOperation = redisTemplate.opsForValue()
        def key = "stringKey"
        def value = "hello"

        when :
        valueOperation.set(key, value)

        then :
        def result = valueOperation.get(key)
        result == value

    }

    def "RedisTemplate set Operation"(){
        given :
        def setOperations = redisTemplate.opsForSet()
        def key = "setKey"

        when :
        setOperations.add(key, "h", "e", "l", "l", "o")

        then :
        def size = setOperations.size(key)
        size == 4

    }

    def "RedisTemplate hash operations"() {
        given:
        HashOperations<String, String, String> hashOperations = redisTemplate.opsForHash()
        String key = "hashKey"

        when:
        hashOperations.put(key, "subKey", "value")

        then:
        String value = hashOperations.get(key, "subKey")
        value == "value"

        Map<String, String> entries = hashOperations.entries(key)
        entries.keySet().contains("subKey")
        entries.values().contains("value")

        Long size = hashOperations.size(key)
        size == entries.size()
    }

}