import React, { useEffect, useState } from 'react';
import PropTypes from 'prop-types';
import { useHistory, useLocation } from 'react-router-dom';
import useMediaQuery from '@material-ui/core/useMediaQuery';
import styled from 'styled-components';
import ComponentError from '../../../../../errorBoundaries/ComponentError/component-error';
import sectionHeaderBg from '../../../../../assets/Banner_img.png';
import { BackArrow } from '../../../../../assets/SvgIcons';
import mediaBreakpoints from '../../../../../breakpoints';
import SelectionTabs from '../Tabs';
import SafeDetailHeader from '../SafeDetailHeader';

// styled components goes here
const Section = styled('section')`
  flex-direction: column;
  display: flex;
  z-index: 2;
  width: 100%;
  height: 100%;
`;

const BackButton = styled.div`
  display: flex;
  align-items: center;
  padding: 2rem 0 0 2rem;
  cursor: pointer;
  span {
    margin-left: 1rem;
  }
`;

const SafeDetails = (props) => {
  const { detailData, resetClicked, goodToRoute, refresh } = props;
  const [safe, setSafe] = useState({});
  // use history of page
  const history = useHistory();
  const location = useLocation();
  // screen view handler
  const isMobileScreen = useMediaQuery(mediaBreakpoints.small);
  // route component data
  const goBackToSafeList = () => {
    resetClicked();
  };

  useEffect(() => {
    // if (goodToRoute && !location?.state?.safe) {
    if (goodToRoute) {
      if (detailData && detailData.length) {
        const activeSafeDetail = detailData.filter(
          (item) =>
            item?.name?.toLowerCase() ===
            history.location.pathname.split('/')[2]
        );
        setSafe(activeSafeDetail[0]);
      }
      // return;
    }
    // if (goodToRoute) setSafe(location?.state?.safe);
  }, [location, goodToRoute, detailData, history.location.pathname]);
  // }, [location.state, detailData, history.location.pathname]);

  return (
    <ComponentError>
      <Section>
        {isMobileScreen ? (
          <BackButton onClick={goBackToSafeList}>
            <BackArrow />
            <span>{(safe && safe.name) || 'No safe'}</span>
          </BackButton>
        ) : null}

        <SafeDetailHeader
          title={safe?.name}
          description={safe?.description}
          bgImage={sectionHeaderBg}
        />

        <SelectionTabs safeDetail={safe} refresh={refresh} />
      </Section>
    </ComponentError>
  );
};
SafeDetails.propTypes = {
  detailData: PropTypes.arrayOf(PropTypes.any),
  resetClicked: PropTypes.func,
  goodToRoute: PropTypes.bool,
  refresh: PropTypes.func,
};
SafeDetails.defaultProps = {
  detailData: [],
  resetClicked: () => {},
  goodToRoute: false,
  refresh: () => {},
};

export default SafeDetails;