/*
 * MIT License
 *
 * Copyright (c) 2020 Ben Davies
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package uk.co.bjdavies.command;

import discord4j.core.spec.EmbedCreateSpec;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.EmitterProcessor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxProcessor;
import reactor.core.publisher.Mono;
import uk.co.bjdavies.api.command.ICommandResponse;
import uk.co.bjdavies.api.command.IResponse;
import uk.co.bjdavies.command.response.ResponseHandler;
import uk.co.bjdavies.command.response.ResponseHandlerFactory;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.function.Consumer;

/**
 * @author ben.davies99@outlook.com (Ben Davies)
 * @since 1.2.7
 */
@Slf4j
public class CommandResponse implements ICommandResponse {

    private final FluxProcessor<IResponse, IResponse> processor;

    public CommandResponse() {
        log.info("Constructor");
        processor = EmitterProcessor.create();
    }

    @Override
    public boolean sendEmbed(Consumer<EmbedCreateSpec> embed) {
        return send(createEmbedType(), embed);
    }

    @Override
    public boolean sendEmbed(Mono<Consumer<EmbedCreateSpec>> embed) {
        return send(createMonoType(createEmbedType()), embed);
    }

    @Override
    public boolean sendEmbed(Flux<Consumer<EmbedCreateSpec>> embed) {
        return send(createFluxType(createEmbedType()), embed);
    }

    @Override
    public boolean sendString(String string) {
        return send(string.getClass(), string);
    }

    @Override
    public boolean sendString(Mono<String> string) {
        return send(createMonoType(String.class), string);
    }

    @Override
    public boolean sendString(Flux<String> string) {
        return send(createFluxType(String.class), string);
    }


    private ParameterizedType createFluxType(Type typeOfFlux) {
        return createType(Flux.class, typeOfFlux);
    }

    private ParameterizedType createMonoType(Type typeOfMono) {
        return createType(Mono.class, typeOfMono);
    }

    private ParameterizedType createEmbedType() {
        return createType(Consumer.class, EmbedCreateSpec.class);
    }

    private ParameterizedType createType(Type raw, Type arg) {
        return new ParameterizedType() {
            @Override
            public Type[] getActualTypeArguments() {
                return new Type[]{arg};
            }

            @Override
            public Type getRawType() {
                return raw;
            }

            @Override
            public Type getOwnerType() {
                return null;
            }
        };
    }


    @Override
    public boolean send(Type type, Object obj) {

        ResponseHandler responseHandler = ResponseHandlerFactory.getHandler(type, processor);

        if (responseHandler != null) {
            responseHandler.handle(obj);
            return true;
        }

        processor.onComplete();
        log.error("Unable to send Object of type: " + type + ", through the command dispatcher.");
        return false;
    }

    @Override
    public FluxProcessor<IResponse, IResponse> getResponses() {
        return processor;
    }

}
