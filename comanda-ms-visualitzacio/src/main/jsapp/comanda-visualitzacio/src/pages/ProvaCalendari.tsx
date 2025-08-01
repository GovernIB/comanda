import FullCalendar from '@fullcalendar/react';
import dayGridPlugin from '@fullcalendar/daygrid';
import interactionPlugin from '@fullcalendar/interaction';
import { useBaseAppContext, GridPage } from 'reactlib';

const ProvaCalendari = () => {
    const { currentLanguage } = useBaseAppContext();
    return (
        <GridPage>
            <FullCalendar
                locale={currentLanguage}
                firstDay={1}
                plugins={[dayGridPlugin, interactionPlugin]}
                initialView="dayGridMonth"
                // headerToolbar={false}
                height={'100%'}
                selectable
                select={(arg) => console.log('Select', arg)}
            />
        </GridPage>
    );
};

export default ProvaCalendari;
