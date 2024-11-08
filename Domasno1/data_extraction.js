const axios = require('axios');
const cheerio = require('cheerio');

// Define the base URL and a function to generate URLs for each issuer
const BASE_URL = 'https://www.mse.mk/mk/stats/symbolhistory/';

async function getIssuerData(issuerCode) {
    const url = `${BASE_URL}${issuerCode.toLowerCase()}`; // lowercase as shown in the example URL
    try {
        const response = await axios.get(url);
        const $ = cheerio.load(response.data);

        // Here, you'd parse the data you need from the HTML
        // Example of fetching data rows, assuming data is in a table
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

const issuerCodes = ['kmb', 'alk','adin','alkb'];
issuerCodes.forEach(async (issuerCode) => {
    const data = await getIssuerData(issuerCode);
    console.log(`Data for ${issuerCode}:`, data);
});
