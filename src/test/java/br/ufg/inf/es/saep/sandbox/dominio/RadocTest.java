package br.ufg.inf.es.saep.sandbox.dominio;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

public class RadocTest {

    private List<Relato> relatos;

    @Before
    public void setUp() {
        Map<String, Valor> valores = new HashMap<String, Valor>();
        valores.put("ano", new Valor(2016));

        relatos = new ArrayList<Relato>(3);

        relatos.add(new Relato("a", valores));
        relatos.add(new Relato("b", valores));
        relatos.add(new Relato("a", valores));
    }

    @Test(expected = CampoExigidoNaoFornecido.class)
    public void idNullGeraExcecao() {
        new Radoc(null, 0, new ArrayList<Relato>());
    }

    @Test(expected = CampoExigidoNaoFornecido.class)
    public void idVazioGeraExcecao() {
        new Radoc("", 0, new ArrayList<Relato>());
    }

    @Test(expected = CampoExigidoNaoFornecido.class)
    public void relatosNullGeraExcecao() {
        new Radoc("id", 0, null);
    }

    @Test
    public void semRelatosBuscaPorTipoRetornaZero() {
        Radoc r = new Radoc("r", 0, new ArrayList<Relato>());
        assertEquals(0, r.relatosPorTipo("qualquer coisa").size());
    }

    @Test
    public void relatosPorTipo() {
        Radoc r = new Radoc("id", 0, relatos);

        assertEquals(0, r.relatosPorTipo("x").size());
        assertEquals(0, r.relatosPorTipo(null).size());
        assertEquals(2, r.relatosPorTipo("a").size());
        assertEquals(1, r.relatosPorTipo("b").size());
    }

    @Test
    public void recuperaCorretamente() {
        Radoc r = new Radoc("x", 1234, new ArrayList<Relato>(0));
        assertEquals("x", r.getId());
        assertEquals(1234, r.getAnoBase());
        assertEquals(0, r.getRelatos().size());
    }
}

