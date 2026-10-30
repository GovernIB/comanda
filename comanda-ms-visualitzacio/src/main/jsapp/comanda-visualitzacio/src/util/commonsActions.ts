
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
    const rawFileName = (result instanceof Blob ? undefined : (result?.name || result?.fileName)) || 'export.csv';
    const fileName = rawFileName.replace(/^["']|["']$/g, '');
    let blob: Blob;

    if (result instanceof Blob || (result && result.constructor?.name === 'Blob') ||
        result?.blob instanceof Blob || (result?.blob && result.blob.constructor?.name === 'Blob') ||
        result?.content instanceof Blob || (result?.content && result.content.constructor?.name === 'Blob')) {
        const blobData = (result instanceof Blob || result?.constructor?.name === 'Blob')
            ? result
            : ((result.blob instanceof Blob || result?.blob?.constructor?.name === 'Blob') ? result.blob : result.content);
        blob = new Blob(['\uFEFF', blobData as Blob], { type: 'text/csv;charset=utf-8;' });
    } else {
        let csvText = '';
        const targetData = result?.content !== undefined ? result.content : result?.blob;
        if (ArrayBuffer.isView(targetData)) {
            const bytes = new Uint8Array(targetData.buffer, targetData.byteOffset, targetData.byteLength);
            csvText = new TextDecoder('utf-8').decode(bytes);
        } else if (targetData instanceof ArrayBuffer || (targetData && targetData.constructor?.name === 'ArrayBuffer')) {
            csvText = new TextDecoder('utf-8').decode(new Uint8Array(targetData));
        } else if (Array.isArray(targetData)) {
            const bytes = new Uint8Array(targetData.map((b: number) => (b < 0 ? b + 256 : b)));
            csvText = new TextDecoder('utf-8').decode(bytes);
        } else if (typeof targetData === 'string') {
            csvText = targetData;
        }
        const textWithBom = csvText.startsWith('\uFEFF') ? csvText : '\uFEFF' + csvText;
        blob = new Blob([textWithBom], { type: 'text/csv;charset=utf-8;' });
    }

    iniciaDescargaBlob({ fileName, blob });
};
