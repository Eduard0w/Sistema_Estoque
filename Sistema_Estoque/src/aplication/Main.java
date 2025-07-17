package aplication;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;

public class Main {
	
	private static final String URL = "jdbc:postgresql://localhost:5432/Projeto_vendas";
	private static final String USER = "postgres";
	private static final String PASSWORD = "postgres123";

	public static void main(String[] args) {
		try(Connection connection = DriverManager.getConnection(URL, USER, PASSWORD)){
		Scanner scanner = new Scanner(System.in);
			while(true) {
				System.out.println("Você quer (1)adicionar, ver (2)produtos ou (3)fechar sistema?");
				int escolha = scanner.nextInt();
				scanner.nextLine();
				
				switch(escolha) {
					case 1:
						addProduto(connection, scanner);
						break;
					case 2:
						verEstoque(connection, scanner);
						break;
					case 3:
						System.out.println("Sistema fechado");
						return;
					default:
						System.out.println("Inválido");
				}
			}
			
		}catch(SQLException e) {
			System.err.println("Erro! Não conectou"+ e.getMessage());
		}
		
	}

	public static void addProduto(Connection connection, Scanner scanner) {
		System.out.println("Digite o nome do produto: ");
		String nomeProduto = scanner.nextLine();
		
		System.out.println("Digite o preço do produto: ");
		Double precoProduto = scanner.nextDouble();
		
		System.out.println("Digite a quantidade do produto que será adicionado: ");
		int qntProduto = scanner.nextInt();
		scanner.nextLine();
		
		String sqlProduto = "INSERT INTO produto (nome, preco) VALUES (?, ?)";
		try(PreparedStatement stmtProduto = connection.prepareStatement(sqlProduto, Statement.RETURN_GENERATED_KEYS)){
			stmtProduto.setString(1, nomeProduto);
			stmtProduto.setDouble(2, precoProduto);
			stmtProduto.executeUpdate();
			
			ResultSet rs = stmtProduto.getGeneratedKeys();//-> está pegando a chave principal do produto gerada automaticamente no SQL
			//ResultSet serve para ler toda a linha criada em SQL e seus resultado.
			if(rs.next()) {//.next() lê cada linha que esta no SQL, uma de cada vez.
				long produtoId = rs.getLong(1);
				
				String sqlEstoque = "INSERT INTO estoque (produto_id, qnt_produto) VALUES (?, ?)";
				try(PreparedStatement stmtEstoque = connection.prepareStatement(sqlEstoque)){
					stmtEstoque.setLong(1, produtoId);
					stmtEstoque.setInt(2, qntProduto);
					stmtEstoque.executeUpdate();
					System.out.println("Produto criado e colocado no estoque com sucesso!");
				}catch(SQLException e){
					System.err.println("ERRO ao cadastrar no estoque: "+ e.getMessage());
				}
			}
			//modificar
			System.out.println("FUNCIONOU");
		}catch(SQLException e) {
			System.err.println("Deu erro na criação do produto " + e.getMessage());
		}
	}
	
	public static void verEstoque(Connection connection, Scanner scanner) {
		
		String sqlVerEstoque = "SELECT p.nome, e.qnt_produto\r\n"
				+ "FROM produto p\r\n"
				+ "JOIN estoque e ON e.produto_id = p.id;";
		try(PreparedStatement stmt = connection.prepareStatement(sqlVerEstoque)){
			ResultSet rs = stmt.executeQuery();
			
			System.out.println("===ESTOQUE===");
			while(rs.next()) {
				String nome = rs.getString("nome");
				int quantidade = rs.getInt("qnt_produto");
				
				System.out.println("Produto: "+ nome +" -- quantidade: "+ quantidade);
			}
		}catch(SQLException e) {
			System.err.println("Ocorreu um erro para ver o estoque: "+ e.getMessage());
		}
	}
}
