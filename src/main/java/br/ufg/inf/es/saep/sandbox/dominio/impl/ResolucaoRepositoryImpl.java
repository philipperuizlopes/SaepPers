package br.ufg.inf.es.saep.sandbox.dominio.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.Query;
import org.hibernate.Session;

import br.ufg.inf.es.saep.sandbox.dominio.Atributo;
import br.ufg.inf.es.saep.sandbox.dominio.CampoExigidoNaoFornecido;
import br.ufg.inf.es.saep.sandbox.dominio.IdentificadorExistente;
import br.ufg.inf.es.saep.sandbox.dominio.Regra;
import br.ufg.inf.es.saep.sandbox.dominio.Resolucao;
import br.ufg.inf.es.saep.sandbox.dominio.ResolucaoRepository;
import br.ufg.inf.es.saep.sandbox.dominio.ResolucaoUsaTipoException;
import br.ufg.inf.es.saep.sandbox.dominio.Tipo;
import br.ufg.inf.es.saep.sandbox.util.HibernateUtil;

public class ResolucaoRepositoryImpl implements ResolucaoRepository {

	@Override
	@SuppressWarnings("unchecked")
	public Resolucao byId(String id) {
		Session session = HibernateUtil.getSession();
		
		Query query = session.createQuery("SELECT NEW MAP (p.id as id, p.dataAprovacao as dataAprovacao, "
				+ "p.nome as nome, p.descricao as descricao) from Resolucao p where p.id = :id ");
		query.setParameter("id", id);
		
		List<Map<String, Object>> resultadosResolucao = (ArrayList<Map<String, Object>>) query.list();
		if (resultadosResolucao.isEmpty()) {
			return null;
		}
		
		return montarResolucao(session, resultadosResolucao.get(0));
	}

	private Resolucao montarResolucao(Session session, Map<String, Object> map) {
		List<Regra> regras = getRegrasResolucao(session, map.get("id").toString());
		
		return new Resolucao(map.get("id").toString(), map.get("nome").toString(), 
				map.get("nome").toString(), (Date) map.get("dataAprovacao"), regras);
	}

	@SuppressWarnings("unchecked")
	private List<Regra> getRegrasResolucao(Session session, String resolucaoId) {
		List<Regra> regras = new ArrayList<Regra>();
		
		Query query = session.createSQLQuery("SELECT id, variavel, tipo, descricao, valor_maximo, valor_minimo "
				+ ", expressao, entao, senao, tipo_relato, pontos_por_item FROM Regra WHERE resolucao_id = :id");
		query.setParameter("id", resolucaoId);
		
		List<Object[]> resultadosRegras = (ArrayList<Object[]>) query.list();
		for (Object[] resultadoRegras : resultadosRegras) {
			query = session.createSQLQuery("SELECT atributo FROM Regra_dependede WHERE regra_id = :id");
			query.setParameter("id", resolucaoId);
			
			List<String> dependeDe = new ArrayList<String>();
			List<Object[]> resultadosDependeDe = (ArrayList<Object[]>) query.list();
			for (Object[] resultadoDependeDe : resultadosDependeDe) {
				dependeDe.add(resultadoDependeDe[0].toString());
			}
			
			String variavel = resultadoRegras[1] != null ? resultadoRegras[1].toString() : null;
            int tipo = resultadoRegras[2] != null ? Integer.parseInt(resultadoRegras[2].toString()) : 0;
            String descricao = resultadoRegras[3] != null ? resultadoRegras[3].toString() : null;
            float valorMaximo = resultadoRegras[4] != null ? Float.parseFloat(resultadoRegras[4].toString()) : 0l;
            float valorMinimo = resultadoRegras[5] != null ? Float.parseFloat(resultadoRegras[5].toString()) : 0l;
            String expressao = resultadoRegras[6] != null ? resultadoRegras[6].toString() : null;
            String entao = resultadoRegras[7] != null ? resultadoRegras[7].toString() : null;
            String senao = resultadoRegras[8] != null ? resultadoRegras[8].toString() : null;
            String tipoRelato = resultadoRegras[9] != null ? resultadoRegras[9].toString() : null;
            float pontosPorItem = resultadoRegras[10] != null ? Float.parseFloat(resultadoRegras[10].toString()) : 0l;
			
			Regra regra = new Regra(variavel, tipo, descricao, valorMaximo, valorMinimo, expressao, entao, senao, tipoRelato, pontosPorItem, dependeDe);
			
			regras.add(regra);
		}
		return regras;
	}

	@Override
	@SuppressWarnings("unchecked")
	public String persiste(Resolucao resolucao) {
		if (resolucao.getId() == null || resolucao.getId().trim().isEmpty()) {
			throw new CampoExigidoNaoFornecido("id");
		}
		Session session = HibernateUtil.getSession();

		Query query = session.createSQLQuery("SELECT id FROM Resolucao WHERE id = :id ");
		query.setParameter("id", resolucao.getId());
		
		List<Object[]> resolucoes = (ArrayList<Object[]>) query.list();
		if (!resolucoes.isEmpty()) {
			session.close();
			throw new IdentificadorExistente("");
		}

		session.beginTransaction();
		session.save(resolucao);
		session.getTransaction().commit();
		session.close();
		
		return resolucao.getId();
	}

	@Override
	public boolean remove(String identificador) {
		Session session = HibernateUtil.getSession();

		session.beginTransaction();
		Resolucao resolucao = byId(identificador);
		if (resolucao != null) {
			session.delete(resolucao);
			session.getTransaction().commit();
			session.close();
			return true;
		}
		session.close();
		return false;
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<String> resolucoes() {
		Session session = HibernateUtil.getSession();
		List<String> ids = new ArrayList<String>();
		
		Query query = session.createSQLQuery("SELECT id FROM Resolucao ");
		
		List<Object[]> resolucoes = (ArrayList<Object[]>) query.list();
		for (Object[] resolucao : resolucoes) {
			ids.add(resolucao[0].toString());
		}
		return ids;
	}

	@Override
	@SuppressWarnings("unchecked")
	public void persisteTipo(Tipo tipo) {
		Session session = HibernateUtil.getSession();

		Query query = session.createSQLQuery("SELECT id FROM Tipo WHERE id = :id ");
		query.setParameter("id", tipo.getId());
		
		List<Object[]> tipos = (ArrayList<Object[]>) query.list();
		if (!tipos.isEmpty()) {
			session.close();
			throw new IdentificadorExistente("");
		}

		session.beginTransaction();
		session.save(tipo);
		session.getTransaction().commit();
		session.close();
	}

	@Override
	@SuppressWarnings("unchecked")
	public void removeTipo(String codigo) {
		Session session = HibernateUtil.getSession();

		Query query = session.createSQLQuery("SELECT id FROM Regra WHERE tipo_relato = :tipo LIMIT 1");
		query.setParameter("tipo", codigo);
		
		List<Object[]> regras = (ArrayList<Object[]>) query.list();
		if (!regras.isEmpty()) {
			session.close();
			throw new ResolucaoUsaTipoException("");
		}

		session.beginTransaction();
		Tipo tipo = tipoPeloCodigo(codigo);
		session.delete(tipo);
		session.getTransaction().commit();
		session.close();
	}

	@Override
	@SuppressWarnings("unchecked")
	public Tipo tipoPeloCodigo(String codigo) {
		Session session = HibernateUtil.getSession();

		Query query = session.createSQLQuery("SELECT id, nome, descricao FROM Tipo WHERE id = :id");
		query.setParameter("id", codigo);
		
		List<Object[]> tipos = (ArrayList<Object[]>) query.list();
		for (Object[] tipo : tipos) {
			query = session.createSQLQuery("SELECT nome, descricao, tipo FROM Atributo WHERE tipo_id = :tipo_id");
			query.setParameter("tipo_id", tipo[0].toString());
			
			Set<Atributo> atributos = new HashSet<Atributo>();
			List<Object[]> atribs = (ArrayList<Object[]>) query.list();
			for (Object[] atrib : atribs) {
				Atributo atributo = new Atributo(atrib[0].toString(), atrib[1].toString(), Integer.parseInt(atrib[2].toString()));
				atributos.add(atributo);
			}
			
			Tipo tipoPorCodigo = new Tipo(tipo[0].toString(), tipo[1].toString(), tipo[2].toString(), atributos);
			session.close();
			return tipoPorCodigo;
		}
		session.close();
		return null;
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<Tipo> tiposPeloNome(String nome) {
		Session session = HibernateUtil.getSession();

		Query query = session.createSQLQuery("SELECT id, nome, descricao FROM Tipo WHERE nome like :nome");
		query.setParameter("nome", "%" + nome + "%");
		
		List<Tipo> tiposPorNome = new ArrayList<Tipo>();
		List<Object[]> tipos = (ArrayList<Object[]>) query.list();
		for (Object[] tipo : tipos) {
			query = session.createSQLQuery("SELECT nome, descricao, tipo FROM Atributo WHERE tipo_id = :tipo_id");
			query.setParameter("tipo_id", tipo[0].toString());
			
			Set<Atributo> atributos = new HashSet<Atributo>();
			List<Object[]> atribs = (ArrayList<Object[]>) query.list();
			for (Object[] atrib : atribs) {
				Atributo atributo = new Atributo(atrib[0].toString(), atrib[1].toString(), Integer.parseInt(atrib[2].toString()));
				atributos.add(atributo);
			}
			
			Tipo tipoPorNome = new Tipo(tipo[0].toString(), tipo[1].toString(), tipo[2].toString(), atributos);
			tiposPorNome.add(tipoPorNome);
		}
		session.close();
		return tiposPorNome;
	}

}
