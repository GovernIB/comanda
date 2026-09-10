
export const iniciaDescarga = (url:string, fileName:string) => {
    const link = document.createElement('a');
    link.href = url;
    link.download = fileName;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link); // Limpieza
    URL.revokeObjectURL(url);
}
export const iniciaDescargaBlob = (result: any) => {
    const url = URL.createObjectURL(result.blob);
    iniciaDescarga(url, result.fileName)
}
export const iniciaDescargaJSON = (result: any) => {
    const data = result.blob;

    const fileName = result.fileName;

    // 1. Convertir el objeto a una cadena JSON
    const jsonStr = JSON.stringify(data, null, 2); // `null, 2` para formato legible

    // 2. Crear un Blob con el contenido
    const blob = new Blob([jsonStr], { type: "application/json" });

    iniciaDescargaBlob({fileName, blob})
}
export const iniciaDescargaCSV = (result: any) => {
    const fileName = result.name || result.fileName || 'historic_estats.csv';
    let csvText = "";
    if (result.content && Array.isArray(result.content)) {
        csvText = result.content.map((byte: number) => String.fromCharCode(byte)).join('');
    }
    else if (result.blob instanceof Blob || result.content instanceof Blob) {
        const blobData = result.blob || result.content;
        const url = URL.createObjectURL(blobData);
        const link = document.createElement('a');
        link.href = url;
        link.download = fileName;
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
        URL.revokeObjectURL(url);
        return;
    }
    else if (typeof result.content === 'string') {
        csvText = result.content;
    }
    else if (result.blob && typeof result.blob === 'string') {
        csvText = result.blob;
    }
    const blob = new Blob(['\uFEFF' + csvText], { type: "text/csv;charset=utf-8;" });
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = fileName;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    URL.revokeObjectURL(url);
};
