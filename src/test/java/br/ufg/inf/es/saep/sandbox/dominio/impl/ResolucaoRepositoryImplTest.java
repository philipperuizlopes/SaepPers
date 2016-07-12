package br.ufg.inf.es.saep.sandbox.dominio.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import br.ufg.inf.es.saep.sandbox.dominio.Atributo;
import br.ufg.inf.es.saep.sandbox.dominio.IdentificadorExistente;
import br.ufg.inf.es.saep.sandbox.dominio.Regra;
import br.ufg.inf.es.saep.sandbox.dominio.Resolucao;
import br.ufg.inf.es.saep.sandbox.dominio.ResolucaoRepository;
import br.ufg.inf.es.saep.sandbox.dominio.Tipo;

public class ResolucaoRepositoryImplTest {

	private ResolucaoRepository resolucaoRepository;
	private static List<Regra> regras;

	@Before
	public void setUp() {
		resolucaoRepository = new ResolucaoRepositoryImpl();
		
		Regra r = new Regra("v", 1, "d", 1, 0, "a", null, null, null, 1, new ArrayList<String>());
        regras = new ArrayList<Regra>();
        regras.add(r);
	}

	@Test
	public void persisteTipoDadosValidos() {
		Tipo tipo = montarTipo("Tipo1");

		resolucaoRepository.removeTipo(tipo.getId());
		resolucaoRepository.persisteTipo(tipo);

		assertNotEquals(resolucaoRepository.tipoPeloCodigo(tipo.getId()), null);
	}

	@Test(expected = IdentificadorExistente.class)
	public void persisteTipoComIdExistenteNaBase() {
		Tipo tipo = montarTipo("Tipo2");

		resolucaoRepository.persisteTipo(tipo);
		resolucaoRepository.persisteTipo(tipo);
	}
	
	@Test
	public void removerTipo() {
		Tipo tipo = montarTipo("Tipo3");
		
		resolucaoRepository.persisteTipo(tipo);
		resolucaoRepository.removeTipo(tipo.getId());
	}
	
	private Tipo montarTipo(String id) {
		String tipoId = id;

		Atributo atributo = new Atributo("a", "d", 1);
        Set<Atributo> atrs = new HashSet<Atributo>(0);
        atrs.add(atributo);
		
		return new Tipo(tipoId, "c", "acd", atrs);
	}
	
	@Test
	public void pesquisarTipoPorNome() {
		Atributo atributo = new Atributo("a", "d", 1);
        Set<Atributo> atrs = new HashSet<Atributo>(0);
        atrs.add(atributo);
        
        Tipo tipo1 = new Tipo("Tipo4", "casa", "d", atrs);
        Tipo tipo2 = new Tipo("Tipo5", "apt", "d", atrs);

		resolucaoRepository.persisteTipo(tipo1);
		resolucaoRepository.persisteTipo(tipo2);
		
		List<Tipo> tipos = resolucaoRepository.tiposPeloNome("asa");
		
		assertEquals(tipos.size(), 1);
	}
	
	@Test
	public void persisteResolucaoDadosValidos() {
		Resolucao resolucao = montarResolucao("Res1");
		
		resolucaoRepository.remove(resolucao.getId());
		resolucaoRepository.persiste(resolucao);
		Resolucao resolucao2 = resolucaoRepository.byId(resolucao.getId());
		assertNotEquals(resolucao2, null);
	}

	@Test(expected = IdentificadorExistente.class)
	public void persisteResolucaoComIdExistenteNaBase() {
		Resolucao resolucao = montarResolucao("Par2");
		
		resolucaoRepository.persiste(resolucao);
		resolucaoRepository.persiste(resolucao);
	}
	
	@Test
	public void removerResolucao() {
		Resolucao resolucao = montarResolucao("Par3");
		
		resolucaoRepository.persiste(resolucao);
		assertEquals(resolucaoRepository.remove(resolucao.getId()), true);
	}
	
	private Resolucao montarResolucao(String id) {
		String resolucaoId = id;

		return new Resolucao(resolucaoId, "r", "d", new Date(), regras);
	}
}

