package method_analyze;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import method_analyze.LocalMethodAnalyzer;

/**
 * @author fwk
 * access database ,select data and insert to new table
 * 
 */
public class AccessDB {
	static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";  
    static final String DB_URL = "jdbc:mysql://10.141.221.73:3306/codehub";
    static final String USER = "root";
    static final String PASS = "root";
    static final String querySQL = "SELECT  A.rlid, A.path, A.classname, B.* FROM java_rl_class as A, "
    		+ "java_rl_class_method as B where A.classid=B.classid and B.methodid between ? and ? order by methodid;";
    static final String insertSQl = "insert into java_rl_class_method_new (methodid, classid, methodname, methodsignature, returntype, "
    		+ "modifier, isAbstract, isFinal, isStatic, begin, end) VALUES (?,?,?,?,?,?,?,?,?,?,?)";
    private Connection conn = null;
    private PreparedStatement pStatement = null;
    private PreparedStatement pStatement2 = null;
    static LocalMethodAnalyzer lAnalyzer = new LocalMethodAnalyzer();
    
    
    public static void main(String args[]) {
    	
    	AccessDB a = new AccessDB();
    	List<String[]> r = new ArrayList<>();
		List<String[]> t = new ArrayList<>();
		for(int i = Integer.parseInt(args[0]);i<94278;i++) {
			try {
				System.out.println("----------"+i);
				r = a.fetch(i*1000+1, i*1000+1000);
				t = a.transdata(r);
//				for(String[] j:r) {
//    				String str = String.format("%s",j[0],j[1],j[2],j[3],j[4],j[5],j[6],j[7],j[8],j[9],j[10]);
//    				System.out.println(str);
//    			}
//    			for(String[] j:t) {
//    				String str = String.format("%s,%s,%s,%s,%s,%s,%s,%s,%s,%s",j[0],j[1],j[2],j[3],j[4],j[5],j[6],j[7],j[8],j[9]);
//    				System.out.println(str);
//    			}
				a.insert(t);
			}catch (Exception e) {
				e.printStackTrace();
//				i--;
//				a = new AccessDB();
			}
		}
		a.closeDB();
    }
    
    
    public AccessDB() {
    	try {
			Class.forName("com.mysql.jdbc.Driver");
			conn = DriverManager.getConnection(DB_URL,USER,PASS);
	        pStatement = conn.prepareStatement(querySQL);
	        pStatement2 = conn.prepareStatement(insertSQl);
		} catch (ClassNotFoundException | SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
       
	}
    
    
    /**
     * purpose: fetch data from java_rl_method and calculate the method path
     * return : a String list of (path, classname, methodid, classid, methodname, methodsignature, returntype, modifier, isAbstract, isFinal, isStatic)
     * */ 
    public List<String[]> fetch(int beginid, int endid){
    	try {
    		
			pStatement.setInt(1, beginid);
			pStatement.setInt(2, endid);
	    	ResultSet rs = pStatement.executeQuery();
	    	List<String[]> result = new ArrayList<String[]>();
	    	String release = "";
	    	String path = "";
	    	rs.last();
	    	int endreleaseid = rs.getInt("rlid");
	    	rs.first();
	    	int beginreleaseid = rs.getInt("rlid");
	    	if(beginreleaseid/10000==endreleaseid/10000) {
	    		release = "releases"+Integer.toString((beginreleaseid-1)/10000+1)+"_unzip/";
	    		rs.beforeFirst();
	    		while(rs.next()) {
	    			path = "/home/fdse/data/" + release +rs.getString("path");
	    			//path = rs.getString("path");
	    			//path, classname, methodid, classid, methodname, methodsinature, returntype, modifier, isAbstract, isFinal, isStatic
	    			String[] tem = {path, rs.getString(3), rs.getString(4), rs.getString(5), rs.getString(6), 
	    					rs.getString(7), rs.getString(8), rs.getString(9), rs.getString(10), rs.getString(11),rs.getString(12)};
	    			result.add(tem);
	    		}
	    		return result;
	    	}else {
	    		rs.beforeFirst();
	    		while(rs.next()) {
	    			release = "releases"+Integer.toString((rs.getInt("rlid")-1)/10000+1)+"_unzip/";
	    			path = "/home/fdse/data/" + release +rs.getString("path");
	    			String[] tem = {path, rs.getString(3), rs.getString(4), rs.getString(5), rs.getString(6), 
	    					rs.getString(7), rs.getString(8), rs.getString(9), rs.getString(10), rs.getString(11), rs.getString(12)};
	    			result.add(tem);
	    		}
	    		return result;
	    	}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		} 
    	
    }
    
    
    /**
     * purpose: insert to database by batching
     * 
     * */ 
    public void insert(List<String[]> datas) {
    	try {
			conn.setAutoCommit(false);
			for (String[] data:datas) {
	    		pStatement2.setInt(1, Integer.parseInt(data[0]));
	    		pStatement2.setInt(2, Integer.parseInt(data[1]));
	        	pStatement2.setString(3, data[2]);
	        	pStatement2.setString(4, data[3]);
	        	pStatement2.setString(5, data[4]);
	        	pStatement2.setString(6, data[5]);
	        	pStatement2.setInt(7, Integer.parseInt(data[6]));
	        	pStatement2.setInt(8, Integer.parseInt(data[7]));
	        	pStatement2.setInt(9, Integer.parseInt(data[8]));
	        	pStatement2.setInt(10, Integer.parseInt(data[9]));
	        	pStatement2.setInt(11, Integer.parseInt(data[10]));
	        	pStatement2.addBatch();
	    	}
	    	pStatement2.executeBatch();
	    	conn.commit();
	    	System.out.println("insert success");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
    
    
    /**
     * purpose: Transform the data from origindata like
     * (path, classname, methodid, classid, methodname, methodsinature, returntype, modifier, isAbstract, isFinal, isStatic) ==>
     * (methodid, classid, methodname, methodsinature, returntype, modifier, isAbstract, isFinal, isStatic, begin, end)
     * */ 
    private List<String[]> result = new ArrayList<String[]>();
    public List<String[]> transdata(List<String[]> originData) {	
    	result.clear();
    	int[] tem; //store method line number
    	String[] tem1 = new String[11];
    	for(String[] i:originData) {
    		//System.out.println(i[2]);
    		tem = lAnalyzer.getMethodLineNumber(i[0], i[1], i[5]); //calculate the methed line number of begin & end
    		System.arraycopy(i, 2, tem1, 0, 9);
    		tem1[9] = String.valueOf(tem[0]);  //set begin
    		tem1[10] = String.valueOf(tem[1]);  //set end
    		result.add(tem1.clone());
    	}
		return result;
	}
    
    
    
    public void closeDB() {
    	try {
			pStatement.close();
			pStatement2.close();
			conn.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
    
}
