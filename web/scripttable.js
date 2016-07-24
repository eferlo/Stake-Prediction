var tabulate = function (data,columns) {
  var table = d3.select('#tablerep').append('table')
	var thead = table.append('thead')
	var tbody = table.append('tbody')

	thead.append('tr')
	  .selectAll('th')
	    .data(columns)
	    .enter()
	  .append('th')
.attr("align","left")
	    .text(function (d) { return d })

	var rows = tbody.selectAll('tr')
	    .data(data)
	    .enter()
	  .append('tr')

	var cells = rows.selectAll('td')
	    .data(function(row) {
	    	return columns.map(function (column) {
	    		return { column: column, value: row[column] }
	      })
      })
      .enter()
    .append('td')
   .attr("align", function(d) { return "right"; })//.style("font", "10px sans-serif");
      .text(function (d) {var retorno=d.value; if (d.column!='cambio') {if (retorno==0) retorno="Baja";  if (retorno==1) retorno="Sube";}  return retorno; })

  return table;
}

d3.csv(company+'daily.csv',function (data) {
	var columns = ['fecha','cambio','decisiontree','regression','svm']
  tabulate(data,columns)
})
