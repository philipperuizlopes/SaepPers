package br.ufg.inf.es.saep.sandbox.dominio.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import br.ufg.inf.es.saep.sandbox.dominio.IdentificadorDesconhecido;
import br.ufg.inf.es.saep.sandbox.dominio.IdentificadorExistente;
import br.ufg.inf.es.saep.sandbox.dominio.Nota;
import br.ufg.inf.es.saep.sandbox.dominio.Parecer;
import br.ufg.inf.es.saep.sandbox.dominio.ParecerRepository;
import br.ufg.inf.es.saep.sandbox.dominio.Pontuacao;
import br.ufg.inf.es.saep.sandbox.dominio.Radoc;
import br.ufg.inf.es.saep.sandbox.dominio.Relato;
import br.ufg.inf.es.saep.sandbox.dominio.Valor;

public class ParecerRepositoryImplTest {

	private ParecerRepository parecerRepository;
	
	private Pontuacao pontuacao;
	private Pontuacao pontuacaoModificada;
	private Nota nota;
	private Map<String, Valor> valores;

	@Before
	public void setUp() {
		parecerRepository = new ParecerRepositoryImpl();
		pontuacao = new Pontuacao("a", new Valor(3));
		pontuacaoModificada = new Pontuacao("a", new Valor(4));
		
		nota = new Nota(pontuacao, pontuacaoModificada, "Correcao");
		
		valores = new HashMap<String, Valor>();
        valores.put("nome", new Valor("tst"));
	}

	@Test
	public void persisteRadocDadosValidos() {
		Radoc radoc = montarRadoc("Rad1");

		parecerRepository.persisteRadoc(radoc);

		assertNotEquals(parecerRepository.radocById(radoc.getId()), null);
	}

	@Test(expected = IdentificadorExistente.class)
	public void persisteRadocComIdExistenteNaBase() {
		Radoc radoc = montarRadoc("Rad2");

		parecerRepository.persisteRadoc(radoc);
		parecerRepository.persisteRadoc(radoc);
	}
	
	@Test
	public void removerRadoc() {
		Radoc radoc = montarRadoc("Rad3");
		
		parecerRepository.persisteRadoc(radoc);
		parecerRepository.removeRadoc(radoc.getId());
	}
	
	private Radoc montarRadoc(String id) {
		String radocId = id;

		Relato relato = new Relato("docente", valores);
		List<Relato> relatos = new ArrayList<Relato>();
		relatos.add(relato);
		
		return new Radoc(radocId, 2016, relatos);
	}
	
	@Test
	public void persisteParecerDadosValidos() {
		Parecer parecer = montarParecer("Par1");
		
		parecerRepository.persisteParecer(parecer);
		Parecer parecer2 = parecerRepository.byId(parecer.getId());
		assertNotEquals(parecer2, null);
	}

	@Test(expected = IdentificadorExistente.class)
	public void persisteParecerComIdExistenteNaBase() {
		Parecer parecer = montarParecer("Par2");
		
		parecerRepository.persisteParecer(parecer);
		parecerRepository.persisteParecer(parecer);
	}
	
	@Test
	public void removerParecer() {
		Parecer parecer = montarParecer("Par3");
		
		parecerRepository.persisteParecer(parecer);
		parecerRepository.removeParecer(parecer.getId());
	}
	
	private Parecer montarParecer(String id) {
		String parecerId = id;
		String resolucaoId = "Res1";

		String radocId = "Rad1";
		List<String> radocsIds = new ArrayList<String>();
		radocsIds.add(radocId);

		List<Pontuacao> pontuacoes = new ArrayList<Pontuacao>();
		pontuacoes.add(pontuacao);

		String fundamentacao = "Promocao";

		return new Parecer(parecerId, resolucaoId, radocsIds,
				pontuacoes, fundamentacao, new ArrayList<Nota>());
	}
	
	@Test
	public void adicionaNotaDadosValidos() {
		String parecerId = "Par1";
		
		int tamanhoAntes = 0;
		int tamanhoDepois = 0;
		
		tamanhoAntes = parecerRepository.byId(parecerId).getNotas().size();
		parecerRepository.adicionaNota(parecerId, nota);
		tamanhoDepois = parecerRepository.byId(parecerId).getNotas().size();
		
		assertNotEquals(tamanhoAntes, tamanhoDepois);
	}
	
	@Test
	public void removeNota() {
		String parecerId = "Par1";

		int tamanhoAntes = 0;
		int tamanhoDepois = 0;

		tamanhoAntes = parecerRepository.byId(parecerId).getNotas().size();
		parecerRepository.adicionaNota(parecerId, nota);
		parecerRepository.removeNota(parecerId, nota.getItemOriginal());
		tamanhoDepois = parecerRepository.byId(parecerId).getNotas().size();

		assertEquals(tamanhoAntes, tamanhoDepois);
	}

	@Test(expected = IdentificadorDesconhecido.class)
	public void adicionaNotaEmParecerDesconhecido() {
		Pontuacao pontuacao = new Pontuacao("a", new Valor(3));

		Nota nota = new Nota(pontuacao, pontuacao, "Correcao");

		parecerRepository.adicionaNota(null, nota);
	}

}
