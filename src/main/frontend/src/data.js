
const CsvData = {
    football: {
        csvHeader: "team:String,wins:Number,losses:Number,win_percentage:Number,total_points_for:Number,total_points_against:Number,net_points_scored:Number,touchdowns:Number,conference:String",
        csvBody:
`Dallas Cowboys,13,3,0.813,421,306,115,49,NFC East
New York Giants,11,5,0.688,310,284,26,36,NFC East
Washington Redskins,8,7,0.531,396,383,13,43,NFC East
Philadelphia Eagles,7,9,0.438,367,331,36,37,NFC East
Green Bay Packers,10,6,0.625,432,388,44,51,NFC North
Detroit Lions,9,7,0.563,346,358,-12,36,NFC North
Minnesota Vikings,8,8,0.5,327,307,20,36,NFC North
Chicago Bears,3,13,0.188,279,399,-120,32,NFC North
Atlanta Falcons,11,5,0.688,540,406,134,63,NFC South
Tampa Bay Buccaneers,9,7,0.563,354,369,-15,41,NFC South
New Orleans Saints,7,9,0.438,469,454,15,55,NFC South
Carolina Panthers,6,10,0.375,369,402,-33,40,NFC South
Seattle Seahawks,10,5,0.656,354,292,62,37,NFC West
Arizona Cardinals,7,8,0.469,418,362,56,51,NFC West
Los Angeles Rams,4,12,0.25,224,394,-170,24,NFC West
San Francisco 49ers,2,14,0.125,309,480,-171,36,NFC West
`
    },
    ithaca_restaurants: {
      csvHeader: "name:String,city:String,state:String,review_count:Number,category:String,price:Number",
      csvBody:
`Just A Taste,Ithaca,NY,266,Tapas Bars,2
Mercato Bar & Kitchen,Ithaca,NY,87,Italian Wine Bars,3
Le Cafe Cent-Dix,Ithaca,NY,37,French,3
Gola Osteria,Ithaca,NY,64,Italian,3
Maxie's Supper Club & Oyster Bar,Ithaca,NY,264,Cajun/Creole Seafood,2
Saigon Kitchen,Ithaca,NY,386,Vietnamese Asian Fusion,2
Istanbul Turkish Kitchen,Ithaca,NY,78,Turkish Middle Eastern Mediterranean,2
ZaZa's Cucina,Ithaca,NY,127,Italian,2
Carriage House Cafe,Ithaca,NY,236,Sandwiches Breakfast & Brunch American (New),2
`
    },
    macbooks: {
      csvHeader: "model:String,inch_display:Number,gigabytes_of_memory:Number,gigabytes_of_storage:Number,dollars:Number,gigahertz:Number,processor:String,hours_battery_life:Number,trackpad:String,pounds:Number",
      csvBody:
`Mac Book One,12,8,256,1300,1.1,Intel Core m3,10,Force Touch,2.03
Mac Book Two,12,8,512,1600,1.2,Intel Core m5,10,Force Touch,2.03
Mac Book Air One,13,8,128,1000,1.6,Intel Core i5,12,Multi-Touch,2.96
Mac Book Air Two,13,8,256,1200,1.6,Intel Core i5,12,Multi-Touch,2.96
Mac Book Pro One,13.3,8,256,1500,2,Intel Core i5,10,Force Touch,3.02
Mac Book Pro Touch Bar One,13.3,8,256,1800,2.9,Intel Core i5,10,Force Touch,3.02
Mac Book Pro Touch Bar Two,13.3,8,512,2000,2.9,Intel Core i5,10,Force Touch,3.02
Mac Book Pro Two,13.3,8,128,1299,2.7,Intel Core i5,10,Force Touch,3.02
Mac Book Pro Touch Bar Three,15,16,256,2400,2.6,Intel Core i7,10,Force Touch,4.02
Mac Book Pro Touch Bar Four,15,16,512,2800,2.7,Intel Core i7,10,Force Touch,4.02
Mac Book Pro Three,15,16,256,2000,2.2,Intel Core i7,10,Force Touch,4.02
`
    },
    restaurants: {
      csvHeader: "restaurant:String,rating:Number,price:Number,cuisine:String",
      csvBody:
`Carmine's Italian Restaurant,4.3,medium,Italian
Havana Central Times Square,4.2,medium,Cuban
Lillie's Victorian Establishment,4.3,medium,British
Times Square Diner & Grill,4.2,low,Diner
Buca di Beppo,3.9,medium,Italian
Toloache,4.2,high,Mexican
Brooklyn Diner,3.8,medium,Diner
Tony's Di Napoli,4.4,medium,Italian
La Masseria,4.3,high,Italian
Dos Caminos,4.0,high,Mexican
`
    },
};

export {CsvData};
