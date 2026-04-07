import { useIsUserAdmin } from '../components/UserContext.ts';

// TODO Reimplementar con una funcionalidad genérica de base-react
const useReadOnlyGestor = () => {
    const isCurrentUserAdmin = useIsUserAdmin();
    return !isCurrentUserAdmin ? true : undefined;
};

export default useReadOnlyGestor;
