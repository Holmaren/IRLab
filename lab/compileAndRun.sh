#!/bin/bash



javac -Xlint:none -encoding ISO-8859-1 -cp .:pdfbox/ ir/*.java

if [ $? -ne 0 ] 
then
	echo "----Failed to compile----"
	exit 1
fi


java -Xmx5120m -cp .:pdfbox/ ir.SearchGUI -d davisWiki/ 

