# Opening Modified LAR Text Files With Excel
Modified LAR is available as a pipe-delimited text file with the .txt extension and has no header row. To read this file in Excel, the following steps would need to be taken.

1. Navigate to the Modified Loan/Application Register (LAR) page [here](https://ffiec.cfpb.gov/data-publication/modified-lar/2018). Enter the institution name in the search box, and click `Download Modified LAR`. 

2. Open Excel. Select the `File` menu, and then `Open`. In the Open menu, double-click on `Browse` and view the Downloads folder. Select the modified LAR file in the Downloads folder as shown below. 

![alt text](https://raw.githubusercontent.com/cfpb/HMDA_Data_Science_Kit/master/documentation_resources/example_images/mlar_tutorial_images/Step2.JPG)

3. A window will open that tells Excel how to read the file. Select `Delimited` in the first window and click `Next.` 

![alt text](https://raw.githubusercontent.com/cfpb/HMDA_Data_Science_Kit/master/documentation_resources/example_images/mlar_tutorial_images/Step3.JPG)

4. In the next window, specify the pipe delimiter. Select `Other` under the `Delimiters` column and place a pipe (`|`) character in the text box field. Click `Next.`

![alt text](https://raw.githubusercontent.com/cfpb/HMDA_Data_Science_Kit/master/documentation_resources/example_images/mlar_tutorial_images/Step4.JPG)

5. In the final window, select `General` for data type, and select `Finish.` 
At this point, the data will populate into their own cells.

![alt text](https://raw.githubusercontent.com/cfpb/HMDA_Data_Science_Kit/master/documentation_resources/example_images/mlar_tutorial_images/Step5.JPG)

6. To add a header, right click on the row number to highlight the top row of data, and select `Insert` to add a blank row. 

![alt text](https://raw.githubusercontent.com/cfpb/HMDA_Data_Science_Kit/master/documentation_resources/example_images/mlar_tutorial_images/Step6.JPG)

7. Select the modified LAR header for the appropriate activity year and open in Excel. These headers are available [here](https://github.com/cfpb/HMDA_Data_Science_Kit/tree/master/documentation_resources/schemas/mlar/headers/). To download from Github, click "Raw" at the top of the file.

![alt text](https://raw.githubusercontent.com/cfpb/HMDA_Data_Science_Kit/master/documentation_resources/example_images/mlar_tutorial_images/Step_7_1.JPG) 

Right click the raw text file page and save as a csv in the Downloads folder. 

![alt text](https://raw.githubusercontent.com/cfpb/HMDA_Data_Science_Kit/master/documentation_resources/example_images/mlar_tutorial_images/Step_7_2.JPG)

8. Highlight the first row of the header file, right click, and select `Copy`. 

![alt text](https://raw.githubusercontent.com/cfpb/HMDA_Data_Science_Kit/master/documentation_resources/example_images/mlar_tutorial_images/Step8.JPG)

9. Navigate back to the modified LAR excel sheet, highlight the first row, right click, and select `Insert Copied Cells`. 

![alt text](https://raw.githubusercontent.com/cfpb/HMDA_Data_Science_Kit/master/documentation_resources/example_images/mlar_tutorial_images/Step9.JPG)
