package com.spring.springwebsockets.sock;

import com.spring.springwebsockets.model.ChatMessage;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.springframework.messaging.Message;
import reactor.core.publisher.FluxSink;

public class SimpleSubscriber implements Subscriber<Message<?>> {

  private FluxSink<Message<ChatMessage>> wsMessageFlux;

/*  public SimpleSubscriber(FluxSink<Message<ChatMessage>> wsMessageFlux) {
    this.wsMessageFlux = wsMessageFlux;
  }*/

  public SimpleSubscriber() {}

  @Override
  public void onSubscribe(Subscription s) {
    // TODO Auto-generated method stub

  }

  @Override
  public void onNext(Message<?> t) {
    System.out.println("Received in Subscriber "+ t);
    //wsMessageFlux.next((Message<ChatMessage>)t);
  }

  @Override
  public void onError(Throwable t) {
    // TODO Auto-generated method stub

  }

  @Override
  public void onComplete() {
    // TODO Auto-generated method stub

  }
}
