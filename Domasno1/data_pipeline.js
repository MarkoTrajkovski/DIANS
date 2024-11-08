const axios = require('axios');
const cheerio = require('cheerio');
const fs = require('fs');  // Add this to require the fs module

// Define the base URL and a function to generate URLs for each issuer
const BASE_URL = 'https://www.mse.mk/mk/stats/symbolhistory/';

async function getIssuerData(issuerCode) {
    const url = `${BASE_URL}${issuerCode.toLowerCase()}`; // lowercase as shown in the example URL
    try {
        const response = await axios.get(url);
        const $ = cheerio.load(response.data);

        // Here, you'd parse the data you need from the HTML
        const data = [];
        $('table tbody tr').each((_, row) => {
            const columns = $(row).find('td');
            const date = $(columns[0]).text().trim();
            const priceOfLastTransaction = $(columns[1]).text().trim();
            const max = $(columns[2]).text().trim();
            const min = $(columns[3]).text().trim();
            const averagePrice = $(columns[4]).text().trim();
            const prom = $(columns[5]).text().trim();
            const volume = $(columns[6]).text().trim();
            const best = $(columns[7]).text().trim();
            const profit = $(columns[8]).text().trim();

            data.push({
                date,
                priceOfLastTransaction,
                max,
                min,
                averagePrice,
                prom,
                volume,
                best,
                profit
            });
        });

        return data;
    } catch (error) {
        console.error(`Error fetching data for issuer ${issuerCode}:`, error);
        return [];
    }
}

// Function to save data to a JSON file
function saveDataToFile(issuerCode, data) {
    const filePath = `./${issuerCode}_data.json`; // File will be saved in the current directory
    fs.writeFile(filePath, JSON.stringify(data, null, 2), (err) => {
        if (err) {
            console.error('Error saving data to file:', err);
        } else {
            console.log(`Data for ${issuerCode} saved to ${filePath}`);
        }
    });
}

// Example usage
const issuerCodes = ['kmb', 'alk','adin','alkb']; // List of issuer codes you want to fetch data for
issuerCodes.forEach(async (issuerCode) => {
    const data = await getIssuerData(issuerCode);
    if (data.length > 0) {
        saveDataToFile(issuerCode, data);  // Save the data to a file
    }
});
