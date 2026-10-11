package br.herdeiros.model.test;

import static org.junit.Assert.*;
import org.junit.Test;

import br.herdeiros.model.Cidade;
import br.herdeiros.model.NomeCidade;
import br.herdeiros.model.Regiao;

public class CidadeTest {

	@Test
	public void testSaquearTesouroPelaPrimeiraVez() 
	{
		// Cidade é criada com 4 tesouros iniciais
		Cidade cidade = new Cidade(NomeCidade.SAMARCANDA, Regiao.PERSIA);
		
		boolean resultado = cidade.saquearTesouro();
		
		assertTrue("Saquear uma cidade pela primeira vez deveria retornar true", resultado);
		assertEquals("Saquear uma cidade pela primeira vez ainda deve ter 3 tesouros", 3, cidade.getQuantidadeTesouros());
		 
	}
	
	@Test
	public void testSaquearUltimoTesouro()
	{
		Cidade cidade = new Cidade(NomeCidade.SAMARCANDA, Regiao.PERSIA);
		//Saqueando a mesma cidade 3 vezes
		int i = 0;
		while(i < 3)
		{
			cidade.saquearTesouro();
			i++;
		}
		boolean resultado = cidade.saquearTesouro();
		
		assertTrue("O saque do último tesouro deveria retornar true", resultado);
		assertTrue("Ao saquear o último tesouro a cidade deveria ser conquistada", cidade.isConquistada());
	}
	
	@Test
	public void testSaquearTesouroSemSucesso()
	{
		Cidade cidade = new Cidade(NomeCidade.SAMARCANDA, Regiao.PERSIA);
		//Saqueando a mesma cidade 3 vezes
		int i = 0;
		while(i < 4)
		{
			cidade.saquearTesouro();
			i++;
		}
		boolean resultado = cidade.saquearTesouro();

		assertFalse("Tentar saquear uma cidade sem tesouros deve retornar false", resultado);
	}

}
