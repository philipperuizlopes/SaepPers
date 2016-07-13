package br.ufg.inf.es.saep.sandbox.dominio.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.Query;
import org.hibernate.Session;

import br.ufg.inf.es.saep.sandbox.dominio.Avaliavel;
import br.ufg.inf.es.saep.sandbox.dominio.ExisteParecerReferenciandoRadoc;
import br.ufg.inf.es.saep.sandbox.dominio.IdentificadorDesconhecido;
import br.ufg.inf.es.saep.sandbox.dominio.IdentificadorExistente;
import br.ufg.inf.es.saep.sandbox.dominio.Nota;
import br.ufg.inf.es.saep.sandbox.dominio.Parecer;
import br.ufg.inf.es.saep.sandbox.dominio.ParecerRepository;
import br.ufg.inf.es.saep.sandbox.dominio.Pontuacao;
import br.ufg.inf.es.saep.sandbox.dominio.Radoc;
import br.ufg.inf.es.saep.sandbox.dominio.Relato;
import br.ufg.inf.es.saep.sandbox.dominio.Valor;
import br.ufg.inf.es.saep.sandbox.util.HibernateUtil;

public class ParecerRepositoryImpl implements ParecerRepository {

	
	public void adicionaNota(String id, Nota nota) {
		Session session = HibernateUtil.getSession();
		
		Parecer parecer = byId(id);
		if (parecer == null) {
			throw new IdentificadorDesconhecido("");
		}
		String identificadorOriginal = "";
		if (nota.getItemOriginal() instanceof Pontuacao) {
			identificadorOriginal = ((Pontuacao) nota.getItemOriginal()).getAtributo();
		} else if (nota.getItemOriginal() instanceof Relato) {
			identificadorOriginal = ((Relato) nota.getItemOriginal()).getTipo();
		}
		for (int i = 0; i < parecer.getNotas().size(); i++) {
			Nota notaEx = parecer.getNotas().get(i);
			if (notaEx.getClass() == nota.getClass() && identificadorOriginal.equals(notaEx)) {
				String identificadorEx = "";
				if (nota.getItemOriginal() instanceof Pontuacao) {
					identificadorEx = ((Pontuacao) nota.getItemOriginal()).getAtributo();
				} else if (nota.getItemOriginal() instanceof Relato) {
					identificadorEx = ((Relato) nota.getItemOriginal()).getTipo();
				}
				if (identificadorOriginal.equals(identificadorEx)) {
					parecer.getNotas().remove(i);
					i--;
				}
			}
		} 
		parecer.getNotas().add(nota);
		session.beginTransaction();
		session.update(parecer);
		session.getTransaction().commit();
		session.close();
	}

	
	@SuppressWarnings("unchecked")
	public void removeNota(String id, Avaliavel original) {
		Session session = HibernateUtil.getSession();

		session.beginTransaction();
		
		String idOriginal = null;
		
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT orig.id, orig.atributo, orig.tipo  FROM Avaliavel orig  ");
		sql.append("INNER JOIN Nota n ON n.original_id = orig.id ");
		sql.append("WHERE n.parecer_id = :id AND orig.type = :tipoAval");
		Query query = session.createSQLQuery(sql.toString());
		query.setParameter("id", id);
		
		if (original instanceof Pontuacao) {
			query.setParameter("tipoAval", "PONTUACAO");
			List<Object[]> origs = (ArrayList<Object[]>) query.list();
			for (Object[] orig : origs) {
				if (orig[1].toString().equals(((Pontuacao) original).getAtributo())) {
					idOriginal = orig[0].toString();
					continue;
				}
			}
		} else if (original instanceof Relato) {
			query.setParameter("tipoAval", "RELATO");
			List<Object[]> origs = (ArrayList<Object[]>) query.list();
			for (Object[] orig : origs) {
				if (orig[2].toString().equals(((Relato) original).getTipo())) {
					idOriginal = orig[0].toString();
					continue;
				}
			}
		}
		if (idOriginal != null) {
			sql = new StringBuilder();
			sql.append("DELETE FROM Nota WHERE original_id = :original AND parecer_id = :id");
			query = session.createSQLQuery(sql.toString());
			query.setParameter("original", idOriginal);
			query.setParameter("id", id);
			query.executeUpdate();
		}
		session.getTransaction().commit();
		session.close();
	}

	
	@SuppressWarnings("unchecked")
	public void persisteParecer(Parecer parecer) {
		Session session = HibernateUtil.getSession();

		Query query = session.createSQLQuery("SELECT id FROM Parecer WHERE id = :id ");
		query.setParameter("id", parecer.getId());
		
		List<Object[]> pareceres = (ArrayList<Object[]>) query.list();
		if (!pareceres.isEmpty()) {
			session.close();
			throw new IdentificadorExistente("");
		}

		session.beginTransaction();
		session.save(parecer);
		session.getTransaction().commit();
		session.close();
	}

	
	public void atualizaFundamentacao(String parecer, String fundamentacao) {
		Parecer parecerOriginal = byId(parecer);
		if (parecerOriginal == null) {
			throw new IdentificadorDesconhecido("");
		}
		
		Parecer parecerAtualizado = new Parecer(parecerOriginal.getId(),
												parecerOriginal.getResolucao(),
												parecerOriginal.getRadocs(),
												parecerOriginal.getPontuacoes(),
												fundamentacao,
												parecerOriginal.getNotas());
		
		persisteParecer(parecerAtualizado);
	}

	
	@SuppressWarnings("unchecked")
	public Parecer byId(String id) {
		Session session = HibernateUtil.getSession();
		
		Query query = session.createQuery("SELECT NEW MAP (p.id as id, p.resolucao as resolucao, p.fundamentacao as fundamentacao) from Parecer p where p.id = :id ");
		query.setParameter("id", id);
		
		List<Map<String, Object>> resultadosParecer = (ArrayList<Map<String, Object>>) query.list();
		if (resultadosParecer.isEmpty()) {
			return null;
		}
		
		return montarParecer(session, resultadosParecer.get(0));
	}

	private Parecer montarParecer(Session session, Map<String, Object> map) {
		List<String> radocs = getRadocsParecer(session, map.get("id").toString());
		List<Pontuacao> pontuacoes = getPontuacoesParecer(session, map.get("id").toString());
		List<Nota> notas = getNotasParecer(session, map.get("id").toString());
		
		return new Parecer(map.get("id").toString(), map.get("resolucao").toString(), radocs, pontuacoes, map.get("fundamentacao").toString(), notas);
	}

	@SuppressWarnings("unchecked")
	private List<String> getRadocsParecer(Session session, String parecerId) {
		List<String> radocs = new ArrayList<String>();
		
		Query query = session.createSQLQuery("SELECT radoc_id FROM Parecer_radocs WHERE parecer_id = :id");
		query.setParameter("id", parecerId);
		
		List<String> resultadosRadoc = (ArrayList<String>) query.list();
		for (String resultadoRadoc : resultadosRadoc) {
			radocs.add(resultadoRadoc);
		}
		return radocs;
	}
	
	@SuppressWarnings("unchecked")
	private List<Pontuacao> getPontuacoesParecer(Session session, String parecerId) {
		List<Pontuacao> pontuacoes = new ArrayList<Pontuacao>();
		
		Query query = session.createSQLQuery("SELECT valor_id,atributo FROM Avaliavel WHERE parecer_id = :id AND type = :type");
		query.setParameter("id", parecerId);
		query.setParameter("type", "PONTUACAO");
		
		List<Object[]> resultadosPontuacao = (ArrayList<Object[]>) query.list();
		
		for (Object[] resultadoPontuacao : resultadosPontuacao) {
			Pontuacao pontuacao = montarPontuacao(session, resultadoPontuacao[0].toString(), resultadoPontuacao[1].toString());
			pontuacoes.add(pontuacao);
		}
		
		return pontuacoes;
	}

	@SuppressWarnings("unchecked")
	private Pontuacao montarPontuacao(Session session, String valorId, String atributo) {
		Query query = session.createSQLQuery("SELECT real,string,logico FROM Valor WHERE id = :id");
		query.setParameter("id", valorId);
		
		List<Object[]> resultadosValor = (ArrayList<Object[]>) query.list();
		Object[] resultadoValor = resultadosValor.get(0);
		
		Valor valor;
		if (resultadoValor[0] != null ) {
			valor = new Valor(Float.parseFloat(resultadoValor[0].toString()));
		} else if (resultadoValor[1] != null) {
			valor = new Valor(resultadoValor[1].toString());
		} else {
			valor = new Valor(resultadoValor[2].toString().equals("Y"));
		}
		
		return new Pontuacao(atributo, valor);
	}

	@SuppressWarnings("unchecked")
	private List<Nota> getNotasParecer(Session session, String parecerId) {
		List<Nota> notas = new ArrayList<Nota>();
		
		Query query = session.createSQLQuery("SELECT original_id,novo_id,justificativa FROM Nota WHERE parecer_id = :id");
		query.setParameter("id", parecerId);
		
		List<Object[]> resultadosNota = (ArrayList<Object[]>) query.list();
		for (Object[] resultadoNota : resultadosNota) {
			Avaliavel original = montarAvaliavel(session, resultadoNota[0].toString());
			Avaliavel novo = montarAvaliavel(session, resultadoNota[1].toString());
			Nota nota = new Nota(original, novo, resultadoNota[2].toString());
			notas.add(nota);
		}
		return notas;
	}
	
	@SuppressWarnings("unchecked")
	private Avaliavel montarAvaliavel(Session session, String avaliavelId) {
		Query query = session.createSQLQuery("SELECT type,valor_id,tipo FROM Avaliavel WHERE id = :id");
		query.setParameter("id", avaliavelId);
		
		List<Object[]> resultadosAvaliavel = (ArrayList<Object[]>) query.list();
		for (Object[] resultadoAvaliavel : resultadosAvaliavel) {
			if (resultadoAvaliavel[0].equals("PONTUACAO")) {
				return montarPontuacao(session, resultadoAvaliavel[1].toString(), avaliavelId);
			} else if (resultadoAvaliavel[0].equals("RELATO")) {
				return montarRelato(session, resultadoAvaliavel[2].toString(), avaliavelId);
			}
		}
		return null;
	}

	
	public void removeParecer(String id) {
		Session session = HibernateUtil.getSession();

		session.beginTransaction();
		Parecer parecer = byId(id);
		session.delete(parecer);
		
		removerOrfaosParecer(session);
		
		session.getTransaction().commit();
		session.close();
	}

	private void removerOrfaosParecer(Session session) {
		Query query = session.createSQLQuery("DELETE FROM Parecer_radocs WHERE parecer_id IS NULL");
		query.executeUpdate();
		
		query = session.createSQLQuery("DELETE FROM Nota WHERE parecer_id IS NULL");
		query.executeUpdate();
		
		query = session.createSQLQuery("DELETE FROM Avaliavel WHERE parecer_id IS NULL AND type = :type");
		query.setParameter("type", "PONTUACAO");
		query.executeUpdate();
	}

	
	@SuppressWarnings("unchecked")
	public Radoc radocById(String identificador) {
		Session session = HibernateUtil.getSession();
		
		Query query = session.createQuery("SELECT NEW MAP (rad.id as id, rad.anoBase as anoBase) from Radoc rad where id = :id ");
		query.setParameter("id", identificador);
		
		List<Map<String, Object>> resultadosRadoc = (ArrayList<Map<String, Object>>) query.list();
		if (resultadosRadoc.isEmpty()) {
			return null;
		}
		
		query = session.createSQLQuery("SELECT id,tipo FROM Avaliavel WHERE radoc_id = :id AND type = :type");
		query.setParameter("id", identificador);
		query.setParameter("type", "RELATO");
		
		List<Object[]> resultadosRelato = (ArrayList<Object[]>) query.list();
		
		return montarRadoc(session, resultadosRadoc.get(0), resultadosRelato);
	}

	private Radoc montarRadoc(Session session, Map<String, Object> resultadoRadoc, List<Object[]> resultadosRelato) {
		List<Relato> relatos = new ArrayList<Relato>();
		for (Object [] resultadoRelato : resultadosRelato) {
			Relato relato = montarRelato(session, resultadoRelato[0].toString(), resultadoRelato[1].toString());
			
			relatos.add(relato);
		}
		removerOrfaosRadoc(session);
		session.close();
		return new Radoc(resultadoRadoc.get("id").toString(), Integer.parseInt(resultadoRadoc.get("anoBase").toString()), relatos);
	}

	@SuppressWarnings("unchecked")
	private Relato montarRelato(Session session, String relatoId, String tipo) {
		Query query = session.createSQLQuery("SELECT atributo,real,string,logico FROM Valor WHERE relato_id = :id");
		query.setParameter("id", relatoId);
		
		List<Object[]> resultadosValor = (ArrayList<Object[]>) query.list();
		Map<String, Valor> valores = new HashMap<String, Valor>();
		for (Object[] resultadoValor : resultadosValor) {				
			Valor valor;
			if (resultadoValor[1] != null ) {
				valor = new Valor(Float.parseFloat(resultadoValor[1].toString()));
			} else if (resultadoValor[2] != null) {
				valor = new Valor(resultadoValor[2].toString());
			} else {
				valor = new Valor(resultadoValor[3].toString().equals("Y"));
			}
			valores.put(resultadoValor[0].toString(), valor);
		}
		return new Relato(tipo, valores);
	}

	
	@SuppressWarnings("unchecked")
	public String persisteRadoc(Radoc radoc) {
		Session session = HibernateUtil.getSession();

		Query query = session.createSQLQuery("SELECT id FROM Radoc WHERE id = :id ");
		query.setParameter("id", radoc.getId());
		
		List<Object[]> radocs = (ArrayList<Object[]>) query.list();
		if (!radocs.isEmpty()) {
			session.close();
			throw new IdentificadorExistente("");
		}
		session.beginTransaction();
		session.save(radoc);
		session.getTransaction().commit();
		session.close();
		return radoc.getId();
	}

	
	@SuppressWarnings("unchecked")
	public void removeRadoc(String identificador) {
		Session session = HibernateUtil.getSession();

		Query query = session.createSQLQuery("SELECT radoc_id FROM Parecer_radocs WHERE radoc_id = :id");
		query.setParameter("id", identificador);
		
		List<Object[]> resultadosRadoc = (ArrayList<Object[]>) query.list();
		if (!resultadosRadoc.isEmpty()) {
			throw new ExisteParecerReferenciandoRadoc("");
		}
		
		session.beginTransaction();
		Radoc radoc = radocById(identificador);
		session.delete(radoc);
		
		removerOrfaosRadoc(session);
		
		session.getTransaction().commit();
		session.close();
	}

	private void removerOrfaosRadoc(Session session) {
		Query query = session.createSQLQuery("DELETE FROM Avaliavel WHERE radoc_id IS NULL AND type = :type");
		query.setParameter("type", "RELATO");
		query.executeUpdate();
	}

}
