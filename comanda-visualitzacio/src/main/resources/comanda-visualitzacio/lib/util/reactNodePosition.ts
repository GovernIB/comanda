import React from 'react';

export type ReactElementWithPosition = {
    position: number;
    element: React.ReactElement;
    /*iconElement?: {
        icon: string,

    };*/
}

export const getPositionedReactElementsWithPosition = (
    position: number,
    nodes: ReactElementWithPosition[] | undefined): ReactElementWithPosition[] | undefined => {
    return nodes?.filter(n => n.position === position);
}

export const getReactElementsWithPosition = (
    position: number,
    nodes: ReactElementWithPosition[] | undefined): React.ReactElement[] | undefined => {
    return nodes?.
        filter(n => n.position === position).
        map(n => n.element);
}

export const createReactElementWithPosition = (elements?: React.ReactElement[] | undefined): ReactElementWithPosition[] | undefined => {
    return elements?.map((element: React.ReactElement, i: number) => ({ position: i, element }));
}

export const joinReactElementsWithReactElementsWithPositions = (
    elements: React.ReactElement[],
    elementsWithPosition: ReactElementWithPosition[] | undefined,
    addKeys?: boolean): React.ReactElement[] => {
    for (let i = elements.length; i >= 0; i--) {
        const elementsToInsert = getReactElementsWithPosition(i, elementsWithPosition);
        elementsToInsert && elementsToInsert.length && elements.splice(i, 0, ...elementsToInsert);
    }
    return addKeys ? elements.map((e, i) => React.cloneElement(e, { key: '' + i })) : elements.map(e => e);
}

export const joinReactElementsWithPositionWithReactElementsWithPositions = (
    elements: ReactElementWithPosition[],
    elementsWithPosition: ReactElementWithPosition[] | undefined): ReactElementWithPosition[] => {
    for (let i = elements.length; i >= 0; i--) {
        const elementsToInsert = getPositionedReactElementsWithPosition(i, elementsWithPosition);
        elementsToInsert && elementsToInsert.length && elements.splice(i, 0, ...elementsToInsert);
    }
    return elements.map(e => e);
}