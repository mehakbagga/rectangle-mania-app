/*
 * Copyright 2002-2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

//mport jdk.javadoc.internal.doclets.formats.html.SourceToHTMLConverter;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.MediaType;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Controller
@SpringBootApplication
public class Main {

  @Value("${spring.datasource.url}")
  private String dbUrl;

  @Autowired
  private DataSource dataSource;

  public static void main(String[] args) throws Exception {
    SpringApplication.run(Main.class, args);
  }

  @RequestMapping("/")
  String index() {
    return "index";
  }
  @GetMapping(
    path="/rectangles"
  )
  public String getRectanglesValue(Map<String, Object> model){
    Rectangles rectangles = new Rectangles();
    model.put("rectangles",rectangles);
    return "rectangles";
  }
  @PostMapping(
    path = "/rectangles",
    consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE}
  )
  public String handleBrowserPersonSubmit(Map<String, Object> model,Rectangles rectangles) throws Exception{
    try (Connection connection = dataSource.getConnection()) {
      Statement stmt = connection.createStatement();
      stmt.executeUpdate("CREATE TABLE IF NOT EXISTS seetable (id serial, name varchar(50), color varchar(20), height integer, width integer)");
      String rect = "INSERT INTO seetable (name, color, height, width) VALUES ('" + rectangles.getname() + "','" + rectangles.getcolor() + "','" + rectangles.getheight() + "','" + rectangles.getwidth() + "')";
      stmt.executeUpdate(rect);
      System.out.println(rectangles.getname() + " " + rectangles.getcolor()+" "+rectangles.getheight() + " " +rectangles.getwidth());
      return "redirect:rectangles/success";
    }catch (Exception e) {
      model.put("message", e.getMessage());
      return "error";
    }   
  }

  //This is for the success page
  @GetMapping("/rectangles/success")
  public String getsuccess(){
    return "success";
  }

  //this is for views
  @GetMapping("/rectangles/view")
  public String getTable(Map<String, Object> model){
    try (Connection connection = dataSource.getConnection()) {
      Statement stmt = connection.createStatement();
      ResultSet rs = stmt.executeQuery("SELECT * FROM seetable");

      //HashMap<String, String> output = new HashMap<>();
      Map<String,ArrayList<String>> rect = new HashMap<String,ArrayList<String>>();
       
      while (rs.next()) {
         String name = rs.getString("name");
         String color = rs.getString("color");
         Integer id = rs.getInt("id");
         rect.put(name, new ArrayList<String>());
         rect.get(name).add(color);
         rect.get(name).add(String.valueOf(id));
      }

      System.out.println(rect);
      model.put("records", rect);
      return "view";
    } catch (Exception e) {
      model.put("message", e.getMessage());
      return "error";
    }
  }

  @RequestMapping("/db")
  String db(Map<String, Object> model) {
    try (Connection connection = dataSource.getConnection()) {
      Statement stmt = connection.createStatement();
      stmt.executeUpdate("CREATE TABLE IF NOT EXISTS ticks (tick timestamp)");
      stmt.executeUpdate("INSERT INTO ticks VALUES (now())");
      ResultSet rs = stmt.executeQuery("SELECT tick FROM ticks");

      ArrayList<String> output = new ArrayList<String>();
      while (rs.next()) {
        output.add("Read from DB: " + rs.getTimestamp("tick"));
      }

      model.put("records", output);
      return "db";
    } catch (Exception e) {
      model.put("message", e.getMessage());
      return "error";
    }
  }

  //this is for display
  @GetMapping("/rectangles/view/{pid}")
  public String getSpecificRectangle(Map<String, Object> model, @PathVariable String pid){
    try (Connection connection = dataSource.getConnection()) {
      Statement stmt = connection.createStatement();
      
      ResultSet rs2 = stmt.executeQuery("SELECT name,color,height,width FROM seetable WHERE id ='" + pid + "'");

      Map<String,ArrayList<String>> displayrect = new HashMap<String,ArrayList<String>>();
        
      while (rs2.next()) {
        String name = rs2.getString("name");
        String color = rs2.getString("color");
        Integer width = rs2.getInt("width");
        Integer height = rs2.getInt("height");
        displayrect.put(name, new ArrayList<String>());
        displayrect.get(name).add(color);
        displayrect.get(name).add(String.valueOf(width));
        displayrect.get(name).add(String.valueOf(height));
      }
      System.out.println(displayrect);
      model.put("rect", displayrect);
      return "display";
    } catch (Exception e) {
      model.put("message", e.getMessage());
      return "error";
    }
  }
  
  //Deleting
  @GetMapping("/rectangles/del/{pid}")
  public String getDeleteRectangle(Map<String, Object> model, @PathVariable String pid){
    try (Connection connection = dataSource.getConnection()) {
      Statement stmt = connection.createStatement();
      stmt.executeUpdate("DELETE FROM seetable WHERE id ='" + pid + "'");
      model.put("id", pid);
      return "delete";
    } catch (Exception e) {
      model.put("message", e.getMessage());
      return "error";
    }
  }

  @Bean
  public DataSource dataSource() throws SQLException {
    if (dbUrl == null || dbUrl.isEmpty()) {
      return new HikariDataSource();
    } else {
      HikariConfig config = new HikariConfig();
      config.setJdbcUrl(dbUrl);
      return new HikariDataSource(config);
    }
  }

}
