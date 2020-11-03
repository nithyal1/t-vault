/* eslint-disable no-nested-ternary */
/* eslint-disable react/jsx-wrap-multilines */
/* eslint-disable react/jsx-curly-newline */
import React, { useState, useCallback, useEffect } from 'react';
import { makeStyles } from '@material-ui/core/styles';
import { debounce } from 'lodash';
import Modal from '@material-ui/core/Modal';
import { Backdrop, Typography, InputLabel } from '@material-ui/core';
import Fade from '@material-ui/core/Fade';
import styled, { css } from 'styled-components';
import useMediaQuery from '@material-ui/core/useMediaQuery';
import PropTypes from 'prop-types';
import ButtonComponent from '../../../../../components/FormFields/ActionButton';
import ComponentError from '../../../../../errorBoundaries/ComponentError/component-error';
import certIcon from '../../../../../assets/cert-icon.svg';
import leftArrowIcon from '../../../../../assets/left-arrow.svg';
import mediaBreakpoints from '../../../../../breakpoints';
import PreviewCertificate from '../../CreateCertificates/preview';
import AutoCompleteComponent from '../../../../../components/FormFields/AutoComplete';
import LoaderSpinner from '../../../../../components/Loaders/LoaderSpinner';
import apiService from '../../apiService';
import ConfirmationModal from '../../../../../components/ConfirmationModal';
import Strings from '../../../../../resources';
import { validateEmail } from '../../../../../services/helper-function';

const { small, belowLarge } = mediaBreakpoints;

const ModalWrapper = styled.section`
  background-color: ${(props) => props.theme.palette.background.modal};
  padding: 5.5rem 6rem 6rem 6rem;
  border: none;
  outline: none;
  width: 69.6rem;
  margin: auto 0;
  display: flex;
  flex-direction: column;
  position: relative;
  ${belowLarge} {
    padding: 2.7rem 5rem 3.2rem 5rem;
    width: 57.2rem;
  }
  ${small} {
    width: 100%;
    padding: 2rem;
    margin: 0;
  }
`;

const HeaderWrapper = styled.div`
  display: flex;
  align-items: center;
  ${small} {
    margin-top: 1rem;
  }
`;

const LeftIcon = styled.img`
  display: none;
  ${small} {
    display: block;
    margin-right: 1.4rem;
    margin-top: 0.3rem;
  }
`;
const IconDescriptionWrapper = styled.div`
  display: flex;
  align-items: center;
  margin-bottom: 1.5rem;
  position: relative;
  margin-top: 3.2rem;
`;

const SafeIcon = styled.img`
  height: 5.7rem;
  width: 5rem;
  margin-right: 2rem;
`;

const InputFieldLabelWrapper = styled.div`
  margin-bottom: 3rem;
  position: ${(props) => (props.postion ? 'relative' : '')};
  .MuiSelect-icon {
    top: auto;
    color: ${(props) => props.theme.customColor.primary.color};
  }
`;

const autoLoaderStyle = css`
  position: absolute;
  top: 3rem;
  right: 1rem;
  color: red;
`;

const ContainerOwnerWrap = styled.div`
  font-size: 1.4rem;
`;

const Container = styled.div``;
const Owner = styled.div``;
const Label = styled.span`
  color: ${(props) => props.theme.customColor.label.color};
  margin-right: 0.3rem;
`;

const Value = styled.span``;

const CancelSaveWrapper = styled.div`
  display: ${(props) => (props.showPreview ? 'none' : 'flex')};
  justify-content: flex-end;
  ${small} {
    margin-top: 5.3rem;
  }
  button {
    ${small} {
      height: 4.5rem;
    }
  }
`;

const CancelButton = styled.div`
  margin-right: 0.8rem;
  ${small} {
    margin-right: 1rem;
    width: 100%;
  }
`;

const loaderStyle = css`
  position: absolute;
  left: 50%;
  top: 50%;
  transform: translate(-50%, -50%);
  color: red;
  z-index: 1;
`;

const useStyles = makeStyles((theme) => ({
  select: {
    '&.MuiFilledInput-root.Mui-focused': {
      backgroundColor: '#fff',
    },
  },
  dropdownStyle: {
    backgroundColor: '#fff',
  },
  modal: {
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    overflowY: 'auto',
    padding: '10rem 0',
    [theme.breakpoints.down('xs')]: {
      alignItems: 'unset',
      justifyContent: 'unset',
      padding: '0',
      height: '100%',
    },
  },
}));

const CreateCertificates = (props) => {
  const { onCloseModal, open, certificateData } = props;

  const [owner, setOwner] = useState('');
  const [options, setOptions] = useState([]);
  const [autoLoader, setAutoLoader] = useState(false);
  const classes = useStyles();
  const isMobileScreen = useMediaQuery(small);
  const [openConfirmationModal, setOpenConfirmationModal] = useState(false);
  const [transferOwnerSuccess, setTransferOwnerSuccess] = useState(false);
  const [responseType, setResponseType] = useState(null);
  const [modalDetail, setModalDetail] = useState({
    title: '',
    description: '',
  });
  const [permission, setPermission] = useState(false);
  const [isValidEmail, setIsValidEmail] = useState(false);
  const [emailError, setEmailError] = useState(false);
  const [disabledTransfer, setDisabledTransfer] = useState(true);

  useEffect(() => {
    if (owner?.length > 2) {
      if (!autoLoader) {
        if (options.length === 0 || !options.includes(owner)) {
          setIsValidEmail(false);
        } else {
          setIsValidEmail(true);
        }
      }
    }
  }, [owner, autoLoader, options]);

  useEffect(() => {
    if (emailError || !isValidEmail) {
      setDisabledTransfer(true);
    } else {
      setDisabledTransfer(false);
    }
  }, [emailError, owner, isValidEmail]);

  useEffect(() => {
    if (!certificateData.certificateStatus) {
      setOpenConfirmationModal(true);
      setModalDetail({
        title: 'Certificate Status',
        description: Strings.Resources.noTransferOwnerAvailable,
      });
      setPermission(false);
    } else {
      setPermission(true);
    }
  }, [certificateData]);

  const onTransferOwnerClicked = () => {
    setResponseType(0);
    setDisabledTransfer(true);
    apiService
      .transferOwner(
        certificateData.certType,
        certificateData.certificateName,
        owner
      )
      .then((res) => {
        if (res?.data?.messages && res.data.messages[0]) {
          setModalDetail({
            title: 'Successfull!',
            description: res.data.messages[0],
          });
        }
        setResponseType(null);
        setOpenConfirmationModal(true);
        setTransferOwnerSuccess(true);
      })
      .catch((err) => {
        if (err?.response?.data?.errors && err.response.data.errors[0]) {
          setModalDetail({
            title: 'Error',
            description: err.response.data.errors[0],
          });
        }
        setResponseType(null);
        setTransferOwnerSuccess(false);
        setOpenConfirmationModal(true);
      });
  };

  const callSearchApi = useCallback(
    debounce(
      (value) => {
        setAutoLoader(true);
        apiService
          .getOwnerTransferEmail(value)
          .then((res) => {
            setOptions([]);
            const array = [];
            setAutoLoader(false);
            if (res?.data?.data?.values?.length > 0) {
              res.data.data.values.map((item) => {
                if (item.userEmail) {
                  return array.push(item.userEmail);
                }
                return null;
              });
              setOptions([...array]);
            }
          })
          .catch(() => setAutoLoader(false));
      },
      1000,
      true
    ),
    []
  );

  const onOwnerChange = (e) => {
    if (e) {
      setOwner(e.target.value);
      if (e.target.value && e.target.value?.length > 2) {
        callSearchApi(e.target.value);
        if (validateEmail(owner)) {
          setEmailError(false);
        } else {
          setEmailError(true);
        }
      }
    }
  };

  const onSelected = (e, val) => {
    setOwner(val);
    setEmailError(false);
  };

  const backToTransfer = () => {
    setOpenConfirmationModal(false);
  };

  const onCloseTransferModal = () => {
    if (responseType !== 0) {
      setOwner('');
      onCloseModal(transferOwnerSuccess);
      setOpenConfirmationModal(false);
    }
  };

  const closeModal = () => {
    onCloseModal(transferOwnerSuccess);
    setOpenConfirmationModal(false);
  };

  return (
    <ComponentError>
      <>
        <ConfirmationModal
          open={openConfirmationModal}
          handleClose={
            transferOwnerSuccess
              ? onCloseTransferModal
              : permission
              ? backToTransfer
              : closeModal
          }
          title={modalDetail.title}
          description={modalDetail.description}
          confirmButton={
            <ButtonComponent
              label="Close"
              color="secondary"
              onClick={() =>
                transferOwnerSuccess
                  ? onCloseTransferModal()
                  : permission
                  ? backToTransfer()
                  : closeModal()
              }
              width={isMobileScreen ? '100%' : '38%'}
            />
          }
        />
        {!openConfirmationModal && (
          <Modal
            aria-labelledby="transition-modal-title"
            aria-describedby="transition-modal-description"
            className={classes.modal}
            open={open}
            onClose={() => onCloseTransferModal()}
            closeAfterTransition
            BackdropComponent={Backdrop}
            BackdropProps={{
              timeout: 500,
            }}
          >
            <Fade in={open}>
              <ModalWrapper>
                {responseType === 0 && (
                  <LoaderSpinner customStyle={loaderStyle} />
                )}
                <HeaderWrapper>
                  <LeftIcon
                    src={leftArrowIcon}
                    alt="go-back"
                    onClick={() => onCloseModal()}
                  />
                  <Typography variant="h5">Transfer Ownership</Typography>
                </HeaderWrapper>
                <IconDescriptionWrapper>
                  <SafeIcon src={certIcon} alt="cert-icon" />
                  <ContainerOwnerWrap>
                    <Container>
                      <Label>Container:</Label>
                      <Value>VenafiBin_12345</Value>
                    </Container>
                    <Owner>
                      <Label>Owner Email:</Label>
                      <Value>{certificateData.certOwnerEmailId}</Value>
                    </Owner>
                  </ContainerOwnerWrap>
                </IconDescriptionWrapper>
                <PreviewCertificate
                  dns={certificateData.dnsNames}
                  certificateType={certificateData.certType}
                  applicationName={certificateData.applicationName}
                  certName={certificateData.certificateName}
                  isEditCertificate
                />
                <InputFieldLabelWrapper postion>
                  <InputLabel required>New Owner Email ID</InputLabel>
                  <AutoCompleteComponent
                    options={options}
                    classes={classes}
                    searchValue={owner}
                    name="owner"
                    onSelected={(e, val) => onSelected(e, val)}
                    onChange={(e) => onOwnerChange(e)}
                    placeholder="Email address- Enter min 3 characters"
                    error={owner?.length > 2 && (emailError || !isValidEmail)}
                    helperText={
                      owner?.length > 2 && (emailError || !isValidEmail)
                        ? 'Please enter a valid email address or not available!'
                        : ''
                    }
                  />
                  {autoLoader && (
                    <LoaderSpinner customStyle={autoLoaderStyle} />
                  )}
                </InputFieldLabelWrapper>
                <CancelSaveWrapper>
                  <CancelButton>
                    <ButtonComponent
                      label="Cancel"
                      color="primary"
                      onClick={() => onCloseModal()}
                      width={isMobileScreen ? '100%' : ''}
                    />
                  </CancelButton>
                  <ButtonComponent
                    label="Transfer"
                    color="secondary"
                    disabled={disabledTransfer}
                    onClick={() => onTransferOwnerClicked()}
                    width={isMobileScreen ? '100%' : ''}
                  />
                </CancelSaveWrapper>
              </ModalWrapper>
            </Fade>
          </Modal>
        )}
      </>
    </ComponentError>
  );
};

CreateCertificates.propTypes = {
  certificateData: PropTypes.objectOf(PropTypes.any).isRequired,
  onCloseModal: PropTypes.func.isRequired,
  open: PropTypes.bool.isRequired,
};

export default CreateCertificates;