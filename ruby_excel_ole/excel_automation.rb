require 'win32ole'
xl = WIN32OLE.new("Excel.Application")

puts "Excel failed to start" unless xl

xl.Visible = false

workbook = xl.Workbooks.Add
sheet = workbook.Worksheets(1)

#create some fake data
data_a = []
(1..10).each{|i| data_a.push i }

data_b = []
(1..10).each{|i| data_b.push((rand * 100).to_i) }

#fill the worksheet with the fake data
#showing 3 ways to populate cells with values
(1..10).each do |i|
  sheet.Range("A#{i}").Select
  xl.ActiveCell.Formula = data_a[i-1]
  
  sheet.Range("B#{i}").Formula = data_b[i-1]
  
  cell = sheet.Range("C#{i}")
  cell.Formula = "=A#{i} - B#{i}"
end 

#chart type constants (via http://support.microsoft.com/kb/147803)
xlArea = 1
xlBar = 2
xlColumn = 3
xlLine = 4
xlPie = 5
xlRadar = -4151
xlXYScatter = -4169
xlCombination = -4111
xl3DArea = -4098
xl3DBar = -4099
xl3DColumn = -4100
xl3DLine = -4101 
xl3DPie = -4102
xl3DSurface = -4103
xlDoughnut = -4120

#creating a chart 
chart_object = sheet.ChartObjects.Add(10, 80, 500, 250)
chart = chart_object.Chart
chart_range = sheet.Range("A1", "B10")
chart.SetSourceData(chart_range, nil)
chart.ChartType = xlXYScatter

#get the value from a cell
val = sheet.Range("C1").Value
puts val

#saving as pre-2007 format
excel97_2003_format = -4143 
pwd =  Dir.pwd.gsub('/','\\') << '\\'
#otherwise, it sticks it in default save directory- C:\Users\Sam\Documents on my system
workbook.SaveAs("#{pwd}whatever.xls", excel97_2003_format)

xl.Quit