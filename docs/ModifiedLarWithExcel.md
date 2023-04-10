# Opening Modified LAR Text Files With Excel
Modified LAR is available as a pipe-delimited text file with the .txt extension and has no header row. To read this file in Excel, the following steps would need to be taken.

1. Navigate to the Modified Loan/Application Register (LAR) page [here](https://ffiec.cfpb.gov/data-publication/modified-lar/2018). Enter the institution name in the search box, and click `Download Modified LAR`. If you would like to include the data field heads, select the checkbox `Include File Header`.

2. Open Excel. Select the `File` menu, and then `Open`. In the Open menu, double-click on `Browse` and view the Downloads folder. Select the modified LAR file in the Downloads folder as shown below. 

![alt text](https://raw.githubusercontent.com/cfpb/hmda-platform/master/docs/example_images/mlar_tutorial_images/Step2.JPG)

3. A window will open that tells Excel how to read the file. Select `Delimited` in the first window and click `Next.` 

![alt text](https://raw.githubusercontent.com/cfpb/hmda-platform/master/docs/example_images/mlar_tutorial_images/Step3.JPG)

4. In the next window, specify the pipe delimiter. Select `Other` under the `Delimiters` column and place a pipe (`|`) character in the text box field. Click `Next.`

![alt text](https://raw.githubusercontent.com/cfpb/hmda-platform/master/docs/example_images/mlar_tutorial_images/Step4.JPG)

5. In the final window, select `General` for data type, and select `Finish.` 
At this point, the data will populate into their own cells.

![alt text](https://raw.githubusercontent.com/cfpb/hmda-platform/master/docs/example_images/mlar_tutorial_images/Step5.JPG)
