package projects.dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;//do I need this?? 

import projects.entity.Category;
import projects.entity.Material;
import projects.entity.Project;
import projects.entity.Step;
import projects.exception.DbException;
//import projects.service.Optional;
import projects.service.ProjectService;
import provided.util.DaoBase;

public class ProjectDao extends DaoBase {

	private static final String CATEGORY_TABLE = "category";
	private static final String MATERIAL_TABLE = "material";
	private static final String PROJECT_TABLE = "project";
	private static final String PROJECT_CATEGORY_TABLE = "project_category";
	private static final String STEP_TABLE = "step";

	public Project insertProject(Project project) {
		//@formatter:off 
		String sql = " " //this sql String gives the sql lanugage that will be used to communicate with the database 
		+ "INSERT INTO " + PROJECT_TABLE + "" 
		+ "(project_name, estimated_hours, actual_hours, difficulty, notes) "
		+ "VALUES "
		+ "(?, ?, ?, ?, ?)"; //? is a placeholder for sql 
		//@formatter:on

		try (Connection conn = DbConnection.getConnection()) {//a connection is established 
			startTransaction(conn); //the transaction is started

			try (PreparedStatement stmt = conn.prepareStatement(sql)) {
				
				setParameter(stmt, 1, project.getProjectName(), String.class); //setParameter is a convenience method that allows us to set the various parameters 
				setParameter(stmt, 2, project.getEstimatedHours(), BigDecimal.class);
				setParameter(stmt, 3, project.getActualHours(), BigDecimal.class);
				setParameter(stmt, 4, project.getDifficulty(), Integer.class);
				setParameter(stmt, 5, project.getNotes(), String.class);
				
				stmt.executeUpdate();
				Integer projectId = getLastInsertId(conn, PROJECT_TABLE);
				commitTransaction(conn);
				
				project.setProjectId(projectId);
				return project;
				
				
			} catch (Exception e) { //an exception is caught if it appears and the connection is rolledback before the exception is thrown to the user 
				rollbackTransaction(conn);
				throw new DbException(e);
			}
		} catch (SQLException e) {
			throw new DbException(e);
		}
	}

	public static List<Project> fetchAllProjects() {
		String sql = "SELECT * FROM " + PROJECT_TABLE + " ORDER BY project_name";
		try(Connection conn = DbConnection.getConnection();){
			startTransaction(conn);//made startTransaction into static to avoid compilation error
			
			try(PreparedStatement stmt = conn.prepareStatement(sql)){
				
				try(ResultSet rs = stmt.executeQuery()) {
					List<Project> listOfProjects = new LinkedList<>();
				
					while (rs.next()) {
						listOfProjects.add(extract(rs, Project.class)); //made method static in DaoBase to avoid compilation error 
					}
				return listOfProjects;
				}
				
			} catch (Exception e) {
				rollbackTransaction(conn);
				throw new DbException(e);
			}
		} catch (SQLException e) {
			throw new DbException(e);
		}
		
	}

	public Optional<Project> fetchProjectById(Integer projectId) {
		String sql = "SELECT * FROM " + PROJECT_TABLE + " WHERE project_id = ?"; //sql statement 
		
		try(Connection conn = DbConnection.getConnection()) {
			startTransaction(conn);//starts the transaction using a try with resource block 
			
			try {
			Project project = null;
			
				try (PreparedStatement stmt = conn.prepareStatement(sql)) {//uses the sql statement created to input into a new prepared statement 
					setParameter(stmt, 1, projectId, Integer.class);//sets parameters for the prepared statement 
				
					try(ResultSet rs = stmt.executeQuery()){
						if(rs.next()) {
						project = extract(rs, Project.class);
					}
				}
			}
			
			if (Objects.nonNull(project)) {//checks if the project is null and gets the materials, categories, and steps for the project 
				project.getMaterials().addAll(fetchMaterialsForProject(conn, projectId));
				project.getCategories().addAll(fetchCategoriesForProject(conn, projectId));
				project.getSteps().addAll(fetchStepsForProject(conn, projectId)); 
			}
			
			commitTransaction(conn);//commits the transaction
			
			return Optional.ofNullable(project);
			} 
			catch (Exception e) {
			rollbackTransaction(conn);//rolls back the connection if an error is found 
			throw new DbException(e);				
			}
			
		} catch (SQLException e) {
			throw new DbException(e);
		}
	}

	private List<Category> fetchCategoriesForProject(Connection conn, Integer projectId) throws SQLException {//fetches categories for the project and adds them to the categories list that is returned 
			//@formatter off 
			String sql = "" + 
			"SELECT c.* FROM " + CATEGORY_TABLE + " c " 
			+ "JOIN " + PROJECT_CATEGORY_TABLE + " pc USING(category_id) "
			+ "WHERE project_id = ?";
			//@formatter on 
		try(PreparedStatement stmt = conn.prepareStatement(sql)) {
			setParameter(stmt, 1, projectId, Integer.class);
			try(ResultSet rs = stmt.executeQuery()){
				List<Category> categories = new LinkedList<>(); 
				
				while(rs.next()) {
					categories.add(extract(rs, Category.class));
				}
				return categories;
			}
		} 	
		}


	private List<Step> fetchStepsForProject(Connection conn, Integer projectId) throws SQLException {//fetches steps for the project and adds them to the steps list that is returned 
			//@formatter off 
			String sql = "" + 
			"SELECT s.* FROM " + STEP_TABLE + " s " 
			+ "WHERE project_id = ?";
			//@formatter on 
		try(PreparedStatement stmt = conn.prepareStatement(sql)) {
			setParameter(stmt, 1, projectId, Integer.class);
			try(ResultSet rs = stmt.executeQuery()){
				List<Step> steps = new LinkedList<>(); 
				
				while(rs.next()) {
					steps.add(extract(rs, Step.class));
				}
				return steps;
			}
		} 	
		}
	

	private List<Material> fetchMaterialsForProject(Connection conn, Integer projectId) throws SQLException {//fetches materials for the project and adds them to the materials list that is returned 
			//@formatter off 
			String sql = "" + 
			"SELECT m.* FROM " + MATERIAL_TABLE + " m " 
			+ "WHERE project_id = ?";
			//@formatter on 
		try(PreparedStatement stmt = conn.prepareStatement(sql)) {
			setParameter(stmt, 1, projectId, Integer.class);
			try(ResultSet rs = stmt.executeQuery()){
				List<Material> materials = new LinkedList<>(); 
				
				while(rs.next()) {
					materials.add(extract(rs, Material.class));
				}
				return materials;
			}
		} 	
		}
	}

