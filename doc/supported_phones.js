var phones = new Array(
	"Siemens", "CX65", 0, 2000,
	"Siemens", "S65", 0, 2000,

	"Sony Ericsson", "D750i", 0, 4000,
	"Sony Ericsson", "J300i", 0, 4000,
	"Sony Ericsson", "K300i", 0, 4000,
	"Sony Ericsson", "K310i", 0, 4000,
	"Sony Ericsson", "K320i", 0, 4000,
	"Sony Ericsson", "K510i", 0, 4000,
	"Sony Ericsson", "K600i", 0, 4000,
	"Sony Ericsson", "K610i", 0, 4000,
	"Sony Ericsson", "K700i", 0, 4000,
	"Sony Ericsson", "K750i", 0, 4000,
	"Sony Ericsson", "K790i", 0, 4000,
	"Sony Ericsson", "K800i", 0, 4000,
	"Sony Ericsson", "P800",  0, 4000,
	"Sony Ericsson", "S600i", 0, 4000,
	"Sony Ericsson", "S700",  0, 4000,
	"Sony Ericsson", "V630i", 0, 4000,
	"Sony Ericsson", "W550i", 0, 4000,
	"Sony Ericsson", "W700i", 0, 4000,
	"Sony Ericsson", "W710i", 0, 4000,
	"Sony Ericsson", "W800i", 0, 4000,
	"Sony Ericsson", "W810i", 0, 4000,
	"Sony Ericsson", "W850i", 0, 4000,
	"Sony Ericsson", "W900i", 0, 4000,
	"Sony Ericsson", "W950i", 0, 4000,
	"Sony Ericsson", "W990i", 0, 4000,
	"Sony Ericsson", "Z520i", 0, 4000,
	"Sony Ericsson", "Z530i", 0, 4000,
	"Sony Ericsson", "Z550i", 0, 4000,
	"Sony Ericsson", "Z610i", 0, 4000,
	"Sony Ericsson", "Z710i", 0, 4000,
	"Sony Ericsson", "Z1010", 0, 4000,

	"Nokia", "6133", 1, 1000,
	"Nokia", "5300", 1, 1000,
	"Nokia", "5200", 1, 1000,
	"Nokia", "N91", 1, 100000,
	"Nokia", "N95", 1, 1000000,
	"Nokia", "N75", 1, 1000000,
	"Nokia", "6288", 1, 500,
	"Nokia", "6085", 1, 500,
	"Nokia", "8800", 1, 1000,
	"Nokia", "7373", 1, 128,
	"Nokia", "7390", 1, 500,
	"Nokia", "6275i", 1, 500,
	"Nokia", "6151", 1, 500,
	"Nokia", "6080", 1, 128,
	"Nokia", "E50", 1, 1000000,
	"Nokia", "5500", 1, 1000000,
	"Nokia", "N73", 1, 1000000,
	"Nokia", "N93", 1, 1000000,
	"Nokia", "N72", 1, 1000000,
	"Nokia", "2865i", 1, 512,
	"Nokia", "6126", 1, 1000,
	"Nokia", "2610", 1, 150,
	"Nokia", "6131", 1, 1000,
	"Nokia", "6136", 1, 512,
	"Nokia", "6070", 1, 128,
	"Nokia", "6125", 1, 512,
	"Nokia", "6103", 1, 128,
	"Nokia", "6102i", 1, 128,
	"Nokia", "6282", 1, 512,


	"Nokia", "6234", 1, 1000,
	"Nokia", "6233", 1, 1000,
	"Nokia", "N71", 1, 100000,
	"Nokia", "N80", 1, 100000,
	"Nokia", "N92", 1, 100000,
	"Nokia", "6165", 1, 500,
	"Nokia", "2855", 1, 512,
	"Nokia", "7370", 1, 500,
	"Nokia", "7360", 1, 166,
	"Nokia", "E60", 1, 1000000,
	"Nokia", "3250", 1, 100000,
	"Nokia", "6280", 1, 500,
	"Nokia", "6265", 1, 512,

	"Nokia", "6111", 1, 500,
	"Nokia", "6060", 1, 137,
	"Nokia", "6270", 1, 500,
	"Nokia", "N90", 1, 100000,
	"Nokia", "N70", 1, 100000,
	"Nokia", "N91", 1, 100000,

	"Nokia", "5140i", 1, 125,
	"Nokia", "8800", 1, 125,
	"Nokia", "8801", 1, 125,
	"Nokia", "6152", 1, 500,
	"Nokia", "3155i", 1, 500,
	"Nokia", "6155i", 1, 500,
	"Nokia", "3152", 1, 500,
	"Nokia", "3155", 1, 500,
	"Nokia", "6155", 1, 500,
	"Nokia", "6030", 1, 137,
	"Nokia", "6230", 1, 500,
	"Nokia", "6021", 1, 125,


	"Nokia", "6682", 1, 100000,
	"Nokia", "6680", 1, 100000,
	"Nokia", "6101", 1, 166,
	"Nokia", "6102", 1, 166,
	"Nokia", "6681", 1, 100000,
	"Nokia", "6235i", 1, 381,
	"Nokia", "3230", 1, 100000,
	"Nokia", "6020", 1, 125,
	"Nokia", "6670", 1, 100000,
	
	
	
	"Nokia", "7260", 1, 125,
	"Nokia", "7270", 1, 250,
	"Nokia", "6651", 1, 293,
	"Nokia", "6630", 1, 100000,
	"Nokia", "6260", 1, 100000,
	"Nokia", "2650", 1, 63,
	"Nokia", "6170", 1, 250,
	"Nokia", "3220", 1, 125,
	"Nokia", "3587i", 1, 63,
	"Nokia", "3125", 1, 143,
	"Nokia", "6610i", 1, 63,
	"Nokia", "6015", 1, 63,
	"Nokia", "3205", 1, 63
); 

function supported_phones(page, dict, size) {
	go_to(escape(page));
	dict_name = dict;	
	dict_size = size;

	if (dict_size >= 1000) {
		dict_size_text = (dict_size / 1000) + "MB";
	} else {
		dict_size_text = dict_size + "kB";
	}
	
	document.getElementById("dict_size").innerHTML = dict_size_text;
	document.getElementById("dict_name").innerHTML = dict_name;
		
	text = "";
	prevPhone = "";
	companyInserted = false;
	first = true;
	for(i = 0; i < phones.length;i += 4) {
		if (prevPhone != phones[i]) {
			companyInserted = false;
		}
		if (phones[i+3] >= dict_size) {
			if (!companyInserted) {
				if (prevPhone != "") text = text + "<br>";
				text = text + "<span class=\"vendor\">" + phones[i] + "</span>";
				companyInserted = true;
				prevPhone = phones[i];
				first = true;
			}
			if (!first) {
				text = text + ", ";
			} else {
				text = text + " - ";
				first = false;
			}
			text = text + phones[i + 1];
		}
	}
	
	if (text != "") {
		document.getElementById("supported").innerHTML = text;
		document.getElementById("supported_header").style.display = "block";
		document.getElementById("supported").style.display = "block";
	} else {
		document.getElementById("supported_header").style.display = "none";
		document.getElementById("supported").style.display = "none";
	}
	
	text = "";
	prevPhone = "";
	companyInserted = false;
	first = true;
	for(i = 0; i < phones.length;i += 4) {
		if (prevPhone != phones[i]) {
			companyInserted = false;
		}
		if (phones[i+3] < dict_size && phones[i+2] == 1) {
			if (!companyInserted) {
				if (prevPhone != "") text = text + "<br>";
				text = text + "<span class=\"vendor\">" + phones[i] + "</span>";
				companyInserted = true;
				prevPhone = phones[i];
				first = true;
			}
			if (!first) {
				text = text + ", ";
			} else {
				text = text + " - ";
				first = false;
			}
			text = text + phones[i + 1];
		}
	}
	if (text != "") {
		document.getElementById("unsupported").innerHTML = text;
		document.getElementById("unsupported_header").style.display = "block";
		document.getElementById("unsupported").style.display = "block";
	} else {
		document.getElementById("unsupported_header").style.display = "none";
		document.getElementById("unsupported").style.display = "none";
	}
}
