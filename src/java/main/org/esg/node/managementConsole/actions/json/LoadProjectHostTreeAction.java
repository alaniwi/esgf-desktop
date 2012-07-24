package org.esg.node.managementConsole.actions.json;

import org.esg.node.generalUtils.Constants;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

import org.esg.node.managementConsole.beans.TreeNode;
import org.esg.node.managementConsole.utils.SqlQuery;

import com.opensymphony.xwork2.ActionSupport;

/**
 * @author CMCC
 */

public class LoadProjectHostTreeAction extends ActionSupport {

	private static final long serialVersionUID = 1L;
	
	private Integer idUser = 1; // da modificare quando ci sar� la sessione
	private List<TreeNode> projects = null;
	
	public String execute() throws Exception {
		
		Connection conn = null;
		try {
			conn = Constants.DATASOURCE.getConnection();
			PreparedStatement stmt = conn.prepareStatement(SqlQuery.GET_PROJECTS_HOSTS.getSql());
			stmt.setInt(1, idUser);
			ResultSet rs = stmt.executeQuery();
			
			projects = new LinkedList<TreeNode>();
			
			int currentIdProjectValue = 0;
			int currentProjectIndex = -1;
			
			while (rs.next()) {
				
				int idProject = rs.getInt("idproject");
//				int idHost = rs.getInt("idhost");
				
				if (currentIdProjectValue != idProject) {
					
					// new project
					TreeNode projectNode = new TreeNode();
					projectNode.setText(rs.getString("pname"));
					projectNode.setExpanded(false);
					projectNode.setExpandable(true);
					
					// new host
					TreeNode hostNode = new TreeNode();
					hostNode.setText(rs.getString("hname"));
					hostNode.setLeaf(true);
					
					// new hosts list
					List<TreeNode> hostsList = new LinkedList<TreeNode>();
					hostsList.add(hostNode); // host added
					
					projectNode.setChildren(hostsList); // setting hostslist as children of projectNode*/
					projects.add(projectNode); // project added
					
					// values and indexes update
					currentIdProjectValue = idProject;
					currentProjectIndex++;
					
				}
				else {
					
					// new host
					TreeNode hostNode = new TreeNode();
					hostNode.setText(rs.getString("hname"));
					hostNode.setLeaf(true);
					
					projects.get(currentProjectIndex).getChildren().add(hostNode); // setting hostslist as children of projectNode
					
				}
			}
			rs.close();
			stmt.close();
		} catch(SQLException e) {
			return ERROR;
		} finally {
			if(conn != null) conn.close();
		}
		return SUCCESS;
		
	}


	public Integer getIdUser() {
		return idUser;
	}

	public void setIdUser(Integer idUser) {
		this.idUser = idUser;
	}

	public List<TreeNode> getProjects() {
		return projects;
	}

	public void setProjects(List<TreeNode> projects) {
		this.projects = projects;
	}

}
