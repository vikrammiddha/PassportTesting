/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.src.Utils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Vector;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFDataFormat;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 *
 * @author Vikram
 */
public class FileReaderWriter {
    
    private Vector vectorDataExcelXLSX = new Vector();  
    
    public ArrayList<String> populateListWithFileData(Vector vectorData) {
      
      
      ArrayList<String> retList = new ArrayList<String>();
        // Looping every row data in vector
        for(int i=0; i<vectorData.size(); i++) {
            Vector vectorCellEachRowData = (Vector) vectorData.get(i);
            StringBuffer rowData = new StringBuffer();
            // looping every cell in each row
            for(int j=0; j<vectorCellEachRowData.size(); j++) {
                
                if(vectorCellEachRowData.get(j).toString() != null && vectorCellEachRowData.get(j).toString().trim().length() > 0){
                    rowData.append(vectorCellEachRowData.get(j).toString() + "##");
                }
                
            }
            retList.add(rowData.toString());
        }
    
        return retList;
    }
    
      public Vector readDataExcelXLSX(String fileName) {
        Vector vectorData = new Vector();
         
        try {
            FileInputStream fileInputStream = new FileInputStream(fileName);
             
            XSSFWorkbook xssfWorkBook = new XSSFWorkbook(fileInputStream);
             
            // Read data at sheet 0
            XSSFSheet xssfSheet = xssfWorkBook.getSheetAt(0);
             
            Iterator rowIteration = xssfSheet.rowIterator();
             
            // Looping every row at sheet 0
            while (rowIteration.hasNext()) {
                XSSFRow xssfRow = (XSSFRow) rowIteration.next();
                Iterator cellIteration = xssfRow.cellIterator();
                 
                Vector vectorCellEachRowData = new Vector();
                 
                // Looping every cell in each row at sheet 0
                while (cellIteration.hasNext()) {
                    XSSFCell xssfCell = (XSSFCell) cellIteration.next();
                    vectorCellEachRowData.addElement(xssfCell);
                }
                 
                vectorData.addElement(vectorCellEachRowData);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
         
        return vectorData;
    }
      
      public ArrayList<String> readExcelFileData(String fileName){
          
          vectorDataExcelXLSX = readDataExcelXLSX(fileName);
          
          ArrayList<String> retList = populateListWithFileData(vectorDataExcelXLSX);
          
          return retList;
       }
      
     /* public void writeToExcel(String fileName, ArrayList<String> rows){
          try {
                FileOutputStream fileOut = new FileOutputStream(fileName);
                HSSFWorkbook workbook = new HSSFWorkbook();
                HSSFSheet worksheet = workbook.createSheet("Passport Users");
                Short colCounter = 0;
                Short rowCounter = 0;
                
                for(String row : rows){
                    HSSFRow row1 = worksheet.createRow((short) rowCounter++);
                    for(String columnValue : row.split("##")){
                        HSSFCell cellC1 = row1.createCell((short) colCounter++);
                        cellC1.setCellValue(columnValue);
                    }
                }         

                workbook.write(fileOut);
                fileOut.flush();
                fileOut.close();
        } catch (FileNotFoundException e) {
                e.printStackTrace();
        } catch (IOException e) {
                e.printStackTrace();
        }
      }&*/
      
      public void writeToExcel(String fileName, ArrayList<String> rowList){
         
        XSSFWorkbook workBook = new XSSFWorkbook();
        XSSFSheet sheet = workBook.createSheet();
        Vector table=new Vector();
        Integer numOfColumns = 0;
        for(String row : rowList){
            numOfColumns = 0;
            for(String col : row.split("##")){
                table.add(new String(col));
                numOfColumns ++;
            }
            
        }
        
        Iterator rows=table.iterator();
        Enumeration rowsOfVector=table.elements();
        int totalNoOfRows=rowList.size();
        int currentRow=0;
        while (rows.hasNext () && currentRow<totalNoOfRows){
            XSSFRow row =  sheet.createRow(currentRow++);               

            for (int i = 0; i < numOfColumns ; i++) {
                XSSFCell cell=row.createCell(i);
                Object val=rows.next();
                if( val instanceof String){
                    cell.setCellValue(val.toString());
                }
                else if(val instanceof Date){
                    cell.setCellValue((java.sql.Date)val);
                }
                else if(val instanceof Double){
                    cell.setCellValue((Double)val);
                }
            }
        }

        FileOutputStream outPutStream = null;
        try {
            outPutStream = new FileOutputStream(fileName);
            workBook.write(outPutStream);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (outPutStream != null) {
                try {
                    outPutStream.flush();
                    outPutStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
      }
    
}
