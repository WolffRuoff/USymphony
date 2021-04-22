package com.muhlenberg.bot;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.jknack.handlebars.Context;
import com.github.jknack.handlebars.JsonNodeValueResolver;
import com.github.jknack.handlebars.context.FieldValueResolver;
import com.github.jknack.handlebars.context.JavaBeanValueResolver;
import com.github.jknack.handlebars.context.MapValueResolver;
import com.github.jknack.handlebars.context.MethodValueResolver;

public class ObjectToContext {
    public static Context Convert(Object object) throws JsonProcessingException {
        ObjectMapper obj = new ObjectMapper();
        String jsonString = obj.writerWithDefaultPrettyPrinter().writeValueAsString(object);
        JsonNode jsonNode = new ObjectMapper().readValue(jsonString, JsonNode.class);
        Context c = Context.newBuilder(jsonNode).resolver(JsonNodeValueResolver.INSTANCE, JavaBeanValueResolver.INSTANCE,
            FieldValueResolver.INSTANCE, MapValueResolver.INSTANCE, MethodValueResolver.INSTANCE).build();
    
        return c;
      }
}
