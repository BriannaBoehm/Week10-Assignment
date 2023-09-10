package projects;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;

import projects.entity.Project;
import projects.exception.DbException;
import projects.service.ProjectService;

public class ProjectsApp {

//@ formatter:off 
private List<String> operations = List.of(
		"1) Add a project", 
		"2) List projects", 
		"3) Select a project");
//@formatter:on 

private Scanner scanner = new Scanner(System.in);
private ProjectService projectService = new ProjectService();
private Project curProject;

public static void main(String[] args) {

	new ProjectsApp().processUserSelections();
		
	}

private void processUserSelections() {//this method tests the user inputs that are used to select items in the list and continues to show the list of options until exiting the menu 
	boolean done = false; 
	while(!done) {
		try{
			int selection = getUserSelection();
			switch(selection) {//the switch statement tests the user input and directs the code accordingly 
			case -1: 
				done = exitMenu();
				break;
			case 1: 
				createProject();
				break;
			case 2: 
				listProjects();
				break;
			case 3: 
				selectProject();
				break;
			default: 
				System.out.println("\n" + selection + " is not a valid selection. Try again.");
			}
		}
		catch(Exception e) {
			System.out.println("\nError: " + e + " Try again.");
		}
	}
}

private void selectProject() {
	listProjects();
	int projectId = getIntInput("Enter a project ID to select a project"); //calls on the previously established method of getIntInput to get the user input and set it as the value for projectId
	curProject = null; //negates any projects that were previously selected 
	curProject = projectService.fetchProjectById(projectId);
	if(Objects.isNull(curProject)) {
		System.out.println("Invalid project ID selected.");
	}
}

private void listProjects() {
	List<Project> projects = projectService.fetchAllProjects();
	System.out.println("\nProjects:");
	projects.forEach(project -> System.out.println(
			"    " + project.getProjectId() + ": " + project.getProjectName()));
}

private void createProject() {//this method sets the user inputs to specific variables 
	String projectName = getStringInput("Enter the project name");
	BigDecimal estimatedHours = getDecimalInput("Enter the estimated hours");
	BigDecimal actualHours = getDecimalInput("Enter the actual hours");
	
	Integer difficulty = getIntInput("Enter the project difficulty (1-5)");
	
	String notes = getStringInput("Enter the project notes");
	Project project = new Project();
	
	project.setProjectName(projectName);
	project.setEstimatedHours(estimatedHours);
	project.setActualHours(actualHours);
	project.setDifficulty(difficulty);
	project.setNotes(notes);
	
	Project dbProject = projectService.addProject(project);
	System.out.println("You have successfully created project: " + dbProject);
}

private BigDecimal getDecimalInput(String prompt) {//converts string inputs to BigDemical inputs 
	String input = getStringInput(prompt);
	if(Objects.isNull(input)) {
		return null;
	} 
	try {
		return new BigDecimal(input).setScale(2);
	}
	catch(NumberFormatException e) {
		throw new DbException(input + " is not a valid decimal number. Try again.");
	}
}

private int getUserSelection() {//takes in user selection 
	printOperations();
	Integer input = getIntInput("Enter a menu selection");
	return Objects.isNull(input) ? -1 : input;
}

private Integer getIntInput(String prompt) {//converts string inputs to Integer inputs 
	String input = getStringInput(prompt);
	if(Objects.isNull(input)) {
		return null;
	} 
	try {
		return Integer.valueOf(input);
	}
	catch(NumberFormatException e) {
		throw new DbException(input + " is not a valid number. Try again.");
	}
}

private String getStringInput(String prompt) {//gets the string inputs from the user, this is the base method for all inputs 
	System.out.print(prompt + ": ");
	String input = scanner.nextLine();
	return input.isBlank() ? null : input.trim();
}

private void printOperations() {//prints out the list of operations for the menu 
	System.out.println("\nThese are the available selections. Press the Enter key to quit:");
	operations.forEach(line -> System.out.println("   " + line)); //prints every line, could also use an enhanced for loop 
	if(Objects.isNull(curProject)) {
		System.out.println("\nYou are not working with a project.");
	} else {
		System.out.println("\nYou are working with project: " + curProject);
	}
}

private boolean exitMenu() {//exists the menu
	System.out.println("Exiting the menu.");
	return true;
}

}//end of class 
