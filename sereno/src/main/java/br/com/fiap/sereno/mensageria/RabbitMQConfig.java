package br.com.fiap.sereno.mensageria;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;

@Configuration
public class RabbitMQConfig {

	public static final String FILA = "sereno.avaliacoes";
	public static final String ROTEADOR = "sereno.exchange";
	public static final String CHAVE_ROTA = "avaliacao.alterada";

	public static final String FILA_DLQ = "sereno.avaliacoes.dlq";
	public static final String ROTEADOR_DLX = "sereno.exchange.dlx";

	@Bean
	public Queue filaAvaliacoes() {
		return QueueBuilder.durable(FILA)
				.withArgument("x-dead-letter-exchange", ROTEADOR_DLX)
				.withArgument("x-dead-letter-routing-key", FILA_DLQ)
				.build();
	}

	@Bean
	public DirectExchange roteador() {
		return new DirectExchange(ROTEADOR, true, false);
	}

	@Bean
	public Binding binding(Queue filaAvaliacoes, DirectExchange roteador) {
		return BindingBuilder.bind(filaAvaliacoes).to(roteador).with(CHAVE_ROTA);
	}

	@Bean
	public Queue filaDlq() {
		return QueueBuilder.durable(FILA_DLQ).build();
	}

	@Bean
	public DirectExchange roteadorDlx() {
		return new DirectExchange(ROTEADOR_DLX, true, false);
	}

	@Bean
	public Binding bindingDlq(Queue filaDlq, DirectExchange roteadorDlx) {
		return BindingBuilder.bind(filaDlq).to(roteadorDlx).with(FILA_DLQ);
	}

	@Bean
	public MessageConverter jsonMessageConverter(ObjectMapper objectMapper) {
		Jackson2JsonMessageConverter conversor = new Jackson2JsonMessageConverter(objectMapper);
		conversor.setAlwaysConvertToInferredType(true);
		return conversor;
	}

}
