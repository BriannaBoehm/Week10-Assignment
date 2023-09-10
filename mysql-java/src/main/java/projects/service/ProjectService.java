package projects.service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import projects.dao.ProjectDao;
import projects.entity.Project;

public class ProjectService {

private ProjectDao projectDao = new ProjectDao();

	public Project addProject(Project project) {
		return projectDao.insertProject(project);
	}

	public List<Project> fetchAllProjects() { //calls the fetchAllProjects method from the ProjectsDao class/tier 
		return projectDao.fetchAllProjects();
	}

	public Project fetchProjectById(Integer projectId) {//calls the fetchProjectById method from the ProjectsDao class/tier OR throws an exception 
		return projectDao.fetchProjectById(projectId).orElseThrow(() -> new NoSuchElementException( 
						"Project with project ID= " + projectId + " does not exist."));
	}

}
